package com.moneytransfer.idempotent.aspect;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.enums.TransferRequestStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.moneytransfer.idempotent.event.NewTransferRequestEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionBusinessErrorEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionRollbackEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * AOP aspect for handling idempotent transfer requests.
 * This aspect intercepts method calls annotated with {@link IdempotentTransferRequest},processes idempotent transfer requests
 * and ensures that multiple identical requests have the same effect as a single request.
 */
@Component
@Aspect
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ThreadLocal<TransactionStatus> currentTransactionStatus = new ThreadLocal<>();

    /**
     * Handles an idempotent transfer request.
     * This method is executed around the transfer method, annotated with {@link IdempotentTransferRequest}.
     * It ensures that the transfer operation is idempotent.
     *
     * @param proceedingJoinPoint
     * @param idempotentTransferRequest
     * @param newTransferDto
     * @return It retrieves the associated {@link Transfer}.
     * @throws Throwable Throws an exception if an error occurs.
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
     * Handles the request for money transfer, encapsulated in {@link NewTransferDto}.
     * Gets or creates a {@link TransferRequest} resource.
     * Validates the idempotency of the request.
     * For status {@link TransferRequestStatus#IN_PROGRESS}, the target transfer method is executed.
     * For status {@link TransferRequestStatus#COMPLETED}, an existing {@link Transfer} is retrieved, or a business exception is thrown.
     *
     * @param newTransferDto
     * @param proceedingJoinPoint
     * @return a new Transfer
     * @throws Throwable Throws an exception if the request failed e.g., is not associated with a {@link Transfer}.
     */
    private Transfer processIdempotentTransferRequest(final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        var transferRequest = getTransferRequest(newTransferDto);
        validateIdempotent(transferRequest, newTransferDto);
        return switch (transferRequest.getTransferRequestStatus()) {
            case IN_PROGRESS -> {
                transactionManager.commit(currentTransactionStatus.get());
                yield processTransfer(transferRequest, proceedingJoinPoint);
            }
            case COMPLETED -> getTransferOrThrow(transferRequest);
        };
    }

    private TransferRequest getTransferRequest(NewTransferDto newTransferDto) throws InterruptedException, ExecutionException {
        CompletableFuture<TransferRequest> future = new CompletableFuture<>();
        applicationEventPublisher.publishEvent(new NewTransferRequestEvent(newTransferDto, future));
        return future.get();
    }


    /**
     * Validates the idempotency of a new transfer request, encapsulated in {@link NewTransferDto}.
     * The persisted {@link TransferRequest} is converted to a {@link NewTransferDto} to perform the comparison.
     *
     * @param transferRequest
     * @param newTransferDto
     * @throws RequestConflictException
     */
    private void validateIdempotent(final TransferRequest transferRequest, final NewTransferDto newTransferDto) throws RequestConflictException {
        if (!newTransferDto.equals(transferRequest.toNewTransferDto())) {
            var errorMessage = "The JSON body does not match with transferRequest id " + transferRequest.getTransferRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    /**
     * Gets a Transfer entity or throws an exception.
     *
     * @param transferRequest
     * @return Transfer
     * @throws RequestConflictException for a business error.
     */
    private Transfer getTransferOrThrow(final TransferRequest transferRequest) throws RequestConflictException {
        return Optional.ofNullable(transferRequest.getTransfer())
                .orElseThrow(() -> new RequestConflictException(transferRequest.getInfoMessage(), transferRequest.getHttpStatus()));
    }

    /**
     * Executes the intercepted method.
     * A {@link Transfer} object is returned only if the intercepted transfer method operation completed successfully.
     * Completes the {@link TransferRequest}.
     *
     * @param transferRequest
     * @param proceedingJoinPoint
     * @return a new {@link Transfer} object
     * @throws Throwable Propagates the exception thrown by the target transfer method.
     */
    private Transfer processTransfer(final TransferRequest transferRequest, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Transfer transfer = null;
        try {
            transfer = executeTransfer(transferRequest, proceedingJoinPoint);
            createNewTransaction(TransactionDefinition.ISOLATION_DEFAULT);
            applicationEventPublisher.publishEvent(new TransferRequestCompletionSuccessEvent(transferRequest, transfer));
            return transfer;
        } catch (RuntimeException e) {
            if (transfer != null) {
                applicationEventPublisher.publishEvent(new TransferRequestCompletionRollbackEvent(transfer));
            }
            throw e;
        }
    }

    /**
     * Executes the target transfer method and defines its transactional boundaries.
     *
     * @param proceedingJoinPoint Exposes proceed() for the target method execution.
     * @return a new Transfer entity
     * @throws MoneyTransferException if an error occurs during money transfer.
     */
    private Transfer executeTransfer(TransferRequest transferRequest, ProceedingJoinPoint proceedingJoinPoint) throws MoneyTransferException {
        try {
            createNewTransaction(TransactionDefinition.ISOLATION_SERIALIZABLE);
            var transfer = (Transfer) proceedingJoinPoint.proceed();
            transactionManager.commit(currentTransactionStatus.get());
            return transfer;
        } catch (Throwable e) {
            if (e instanceof MoneyTransferException mte) {
                applicationEventPublisher.publishEvent(new TransferRequestCompletionBusinessErrorEvent(transferRequest, mte));
                throw mte;
            }
            throw new RuntimeException(e);
        }
    }

    private void createNewTransaction(int isolation) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        def.setIsolationLevel(isolation);
        def.setName("IdempotentTransferAspectTransaction_" + System.currentTimeMillis());
        TransactionStatus transactionStatus = transactionManager.getTransaction(def);
        currentTransactionStatus.set(transactionStatus);
    }
}
