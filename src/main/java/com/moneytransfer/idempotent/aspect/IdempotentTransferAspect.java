package com.moneytransfer.idempotent.aspect;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.moneytransfer.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * AOP aspect for handling idempotent transfer requests.
 * This aspect intercepts method calls annotated with {@link IdempotentTransferRequest},processes idempotent transfer requests
 * and ensures that multiple identical requests have the same effect as a single request.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    private final RequestService requestService;

    /**
     * Handles an idempotent transfer request.
     * This method is executed around the transfer method, annotated with {@link IdempotentTransferRequest}.
     * It ensures that the transfer operation is idempotent.
     *
     * @param proceedingJoinPoint
     * @param idempotentTransferRequest
     * @param newTransferDto
     * @return It retrieves the associated {@link Transaction}
     * @throws Throwable Throws an exception if a business error occurs during the money transfer operation.
     */
    @Around("@annotation(idempotentTransferRequest) && execution(* transfer(com.moneytransfer.dto.NewTransferDto,..)) && args(newTransferDto,..)")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction handleIdempotentTransferRequest(ProceedingJoinPoint proceedingJoinPoint, IdempotentTransferRequest idempotentTransferRequest, NewTransferDto newTransferDto) throws Throwable {
        return handleRequest(newTransferDto, proceedingJoinPoint);
    }

    /**
     * Handles the request for money transfer, encapsulated in {@link NewTransferDto}.
     * Gets or creates a {@link TransactionRequest} resource.
     * Validates the idempotency of the request.
     * For status {@link com.moneytransfer.enums.TransactionRequestStatus#IN_PROGRESS},
     * the intercepted transfer method is executed.
     * For status {@link com.moneytransfer.enums.TransactionRequestStatus#COMPLETED},
     * an existing {@link Transaction} is retrieved, or an exception is thrown.
     *
     * @param newTransferDto
     * @param proceedingJoinPoint
     * @return a new Transaction
     * @throws Throwable Throws an exception if the request failed e.g., is not associated with a {@link Transaction}.
     */
    private Transaction handleRequest(final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        var request = getOrCreateRequest(newTransferDto);
        validateIdempotent(request, newTransferDto);
        return switch (request.getTransactionRequestStatus()) {
            case IN_PROGRESS -> processTransfer(newTransferDto, proceedingJoinPoint);
            case COMPLETED -> getTransactionOrThrow(request);
        };
    }

    /**
     * Gets or submits a {@link TransactionRequest}.
     *
     * @param newTransferDto
     * @return TransactionRequest
     */
    private TransactionRequest getOrCreateRequest(final NewTransferDto newTransferDto) {
        var transactionRequest = requestService.getRequest(newTransferDto.requestId());
        return Optional.ofNullable(transactionRequest).orElseGet(() -> requestService.createRequest(newTransferDto));
    }

    /**
     * Executes the intercepted method.
     * A {@link Transaction} object is returned only if the intercepted transfer method operation completed successfully.
     * Completes the {@link TransactionRequest}.
     *
     * @param newTransferDto
     * @param proceedingJoinPoint
     * @return a new {@link Transaction} object
     * @throws Throwable Propagates the exception thrown by the intercepted transfer method.
     */
    private Transaction processTransfer(final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Transaction transaction = null;
        HttpStatus httpStatus = null;
        String infoMessage = null;
        try {
            transaction = (Transaction) proceedingJoinPoint.proceed();
            httpStatus = HttpStatus.CREATED;
            infoMessage = "Transfer Request completed successfully.";
            return transaction;
        } catch (MoneyTransferException e) {
            infoMessage = e.getMessage();
            httpStatus = e.getHttpStatus();
            throw e;
        } finally {
            requestService.completeRequest(newTransferDto, transaction, httpStatus, infoMessage);
        }
    }

    /**
     * Gets the persisted Transaction or throws an exception.
     *
     * @param transactionRequest
     * @return Transaction
     * @throws Throwable
     */
    private Transaction getTransactionOrThrow(final TransactionRequest transactionRequest) throws RequestConflictException {
        return Optional.ofNullable(transactionRequest.getTransaction())
                .orElseThrow(() -> new RequestConflictException(transactionRequest.getInfoMessage(), transactionRequest.getHttpStatus()));
    }

    /**
     * Validates the idempotency of a new transfer request, encapsulated in {@link NewTransferDto}.
     * The persisted {@link TransactionRequest} is converted to a {@link NewTransferDto} to perform the comparison.
     *
     * @param transactionRequest
     * @param newTransferDto
     * @throws RequestConflictException
     */
    private void validateIdempotent(final TransactionRequest transactionRequest, final NewTransferDto newTransferDto) throws RequestConflictException {
        if (!newTransferDto.equals(transactionRequest.toNewTransferDto())) {
            var errorMessage = "The JSON body does not match with transactionRequest id " + transactionRequest.getRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }
}
