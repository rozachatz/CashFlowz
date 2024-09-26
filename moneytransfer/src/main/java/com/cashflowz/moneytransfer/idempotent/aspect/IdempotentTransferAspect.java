package com.cashflowz.moneytransfer.idempotent.aspect;

import com.cashflowz.common.events.Event;
import com.cashflowz.common.events.TransferCompletedEvent;
import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.entity.Transfer;
import com.cashflowz.moneytransfer.entity.TransferRequest;
import com.cashflowz.moneytransfer.enums.TransferRequestStatus;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.exceptions.RequestConflictException;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.cashflowz.moneytransfer.service.TransferRequestService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;

/**
 * AOP aspect for handling idempotent transfer requests.
 * This aspect intercepts method calls annotated with {@link IdempotentTransferRequest},processes idempotent transfer requests
 * and ensures that multiple identical requests have the same effect as a single request.
 */
@Component
@Aspect
@RequiredArgsConstructor
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1, topics = {"console-notification"})
public class IdempotentTransferAspect {
    private final PlatformTransactionManager transactionManager;
    private final TransferRequestService transferRequestService;
    private final ThreadLocal<TransactionStatus> currentTransactionStatus = new ThreadLocal<>();
    private final KafkaTemplate<String, Event> kafkaTemplate;
    /**
     * Handles an idempotent request for money transfer, encapsulated by {@link NewTransferDto}.
     * <p>
     * For a given money transfer request, this method will always return:
     * The same {@link Transfer} object for a successful money transfer,
     * or a {@link MoneyTransferException} with the same http status and message for a failed money transfer.
     * </p>
     *
     * <p>
     * The transactional boundaries of the aspect (inherited by the target method) are ignored
     * and a new transaction is programmatically started using {@link PlatformTransactionManager}.
     * </p>
     *
     * @param proceedingJoinPoint       Exposes the money transfer method.
     * @param idempotentTransferRequest The annotation for the {@link IdempotentTransferAspect}.
     * @param newTransferDto            The dto representing the new money transfer request.
     * @return The associated {@link Transfer} object.
     * @throws Throwable If a (business) error occurs.
     */
    @Around("@annotation(idempotentTransferRequest) && args(newTransferDto)")
    public Transfer handleIdempotentTransferRequest(ProceedingJoinPoint proceedingJoinPoint, IdempotentTransferRequest idempotentTransferRequest, NewTransferDto newTransferDto) throws Throwable {
        createNewTransaction(TransactionDefinition.ISOLATION_DEFAULT);
        try {
            Transfer transfer = processIdempotentTransferRequest(newTransferDto, proceedingJoinPoint);
            transactionManager.commit(currentTransactionStatus.get());
            return transfer;
        } catch (RuntimeException e) {
            transactionManager.rollback(currentTransactionStatus.get());
            throw e;
        } finally {
            currentTransactionStatus.remove();
        }
    }

    /**
     * This method creates or retrieves a {@link TransferRequest} and then processes it,
     * depending on its {@link TransferRequestStatus}.
     * <p>
     * For status {@link TransferRequestStatus#IN_PROGRESS}, the first transaction is committed
     * and then the money transfer method and transfer request completion are executed,
     * with their own transactional boundaries.
     * </p>
     * <p>
     * For status {@link TransferRequestStatus#COMPLETED}, the associated {@link Transfer} is retrieved or
     * {@link RequestConflictException} is thrown.
     * </p>
     *
     * @param newTransferDto      The dto representing the new money transfer request.
     * @param proceedingJoinPoint Exposes the money transfer method.
     * @return The associated {@link Transfer} object.
     * @throws Throwable if the request failed due to a (business) error.
     */
    private Transfer processIdempotentTransferRequest(final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        var transferRequest = getAndCompareTransferRequest(newTransferDto);
        return switch (transferRequest.getTransferRequestStatus()) {
            case IN_PROGRESS -> {
                transactionManager.commit(currentTransactionStatus.get());
                yield processTransferRequest(transferRequest, proceedingJoinPoint);
            }
            case COMPLETED -> getTransferOrThrow(transferRequest);
        };
    }

    /**
     * Gets a {@link TransferRequest} with the same id, or creates a new one.
     * A retrieved money transfer request is compared with the payload of the current money transfer request.
     *
     * @param newTransferDto The dto representing the new money transfer request.
     * @return The associated {@link TransferRequest} object.
     */
    private TransferRequest getAndCompareTransferRequest(NewTransferDto newTransferDto) throws RequestConflictException {
        try {
            TransferRequest transferRequest = transferRequestService.getTransferRequest(newTransferDto.transferRequestId());
            compareRequestPayload(transferRequest, newTransferDto);
            return transferRequest;
        } catch (ResourceNotFoundException e) {
            return transferRequestService.createTransferRequest(newTransferDto);
        }
    }


    /**
     * Compares the payloads of the new money transfer request and of the retrieved money transfer request.
     * The retrieved {@link TransferRequest} is converted to a {@link NewTransferDto} to perform the comparison.
     *
     * @param transferRequest The associated {@link TransferRequest}.
     * @param newTransferDto  The dto representing the new money transfer request.
     * @throws RequestConflictException if the payloads do not match.
     */
    private void compareRequestPayload(final TransferRequest transferRequest, final NewTransferDto newTransferDto) throws RequestConflictException {
        if (!newTransferDto.equals(transferRequest.toNewTransferDto())) {
            var errorMessage = "The JSON body does not match with transferRequest id " + transferRequest.getTransferRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    /**
     * Gets a {@link Transfer} associated with a {@link TransferRequest},
     * or throws a {@link RequestConflictException} for an empty {@link Transfer} field.
     * A {@link TransferRequest} associated with an empty {@link Transfer} indicates a failed money transfer,
     * while a non-empty value a successful money transfer.
     *
     * @param transferRequest The retrieved {@link TransferRequest}.
     * @return A {@link Transfer} object.
     * @throws RequestConflictException for a failed money transfer.
     */
    private Transfer getTransferOrThrow(final TransferRequest transferRequest) throws RequestConflictException {
        return Optional.ofNullable(transferRequest.getTransfer())
                .orElseThrow(() -> new RequestConflictException(transferRequest.getInfoMessage(), transferRequest.getHttpStatus()));
    }

    /**
     * This method processes a {@link TransferRequest} with status {@link TransferRequestStatus#IN_PROGRESS}.
     * It proceeds with the money transfer method and then completes the associated {@link TransferRequest}.
     *
     * <p>
     * The transactional boundaries of the money transfer operation and request completion are defined using programmatic transaction management.
     * The money transfer operation requires a new transaction with {@link TransactionDefinition#ISOLATION_SERIALIZABLE} isolation,
     * while the completion of a successful/failed money transfer request requires a new transaction with {@link TransactionDefinition#ISOLATION_DEFAULT}.
     * </p>
     *
     * @param transferRequest     The associated {@link TransferRequest}.
     * @param proceedingJoinPoint Exposes the money transfer method.
     * @return a new {@link Transfer} object.
     * @throws Throwable for a (business) error.
     */
    private Transfer processTransferRequest(final TransferRequest transferRequest, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Transfer transfer;
        try {
            transfer = transferMoney(proceedingJoinPoint);
        } catch (MoneyTransferException mte) {
            createNewTransaction(TransactionDefinition.ISOLATION_DEFAULT);
            transferRequestService.completeFailedTransferRequest(transferRequest, mte.getHttpStatus(), mte.getMessage());
            throw mte;
        }
        createNewTransaction(TransactionDefinition.ISOLATION_DEFAULT);
        transferRequestService.completeSuccessfulTransferRequest(transferRequest, transfer);
        String message = "Transfer with id " +transfer.getTransferId()+" of " +transfer.getAmount()+transfer.getCurrency() +" to account "+ transfer.getTargetAccount().getAccountId()+" was completed successfully";
        kafkaTemplate.send("console-notification", new TransferCompletedEvent(message));
        return transfer;
    }

    /**
     * Executes the target transfer method and defines its transactional boundaries.
     * The money transfer method always has a {@link TransactionDefinition#ISOLATION_SERIALIZABLE}
     * and a {@link TransactionDefinition#PROPAGATION_REQUIRES_NEW}
     * isolation and propagation level, respectively.
     *
     * @param proceedingJoinPoint Exposes the money transfer method.
     * @return a new {@link Transfer} object
     * @throws Throwable if the money transfer operation fails.
     */
    private Transfer transferMoney(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        createNewTransaction(TransactionDefinition.ISOLATION_SERIALIZABLE);
        var transfer = (Transfer) proceedingJoinPoint.proceed();
        transactionManager.commit(currentTransactionStatus.get());
        return transfer;
    }

    /**
     * Programmatic transaction definition and creation.
     * <p>
     * The new transaction will always have a {@link TransactionDefinition#PROPAGATION_REQUIRES_NEW} propagation level.
     * The {@link TransactionStatus} is also saved as a {@link ThreadLocal<TransactionStatus>} entry,
     * to keep track of the current transaction for each thread.
     * </p>
     *
     * @param isolation The isolation level of the new transaction.
     */
    private void createNewTransaction(int isolation) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        def.setIsolationLevel(isolation);
        def.setName("IdempotentTransferAspectTransaction_" + System.currentTimeMillis());
        TransactionStatus transactionStatus = transactionManager.getTransaction(def);
        currentTransactionStatus.set(transactionStatus);
    }
}
