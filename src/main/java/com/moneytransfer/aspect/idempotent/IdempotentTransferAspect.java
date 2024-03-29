package com.moneytransfer.aspect.idempotent;

import com.moneytransfer.annotation.idempotent.IdempotentTransferRequest;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
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

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentTransferAspect {
    private final RequestService requestService;

    /**
     * Processes the idempotent transfer request.
     * @param proceedingJoinPoint
     * @param idempotentTransferRequest
     * @param requestId
     * @param newTransferDto
     * @return a (successful) Transaction
     * @throws Throwable
     */
    @Around("@annotation(idempotentTransferRequest) && execution(* transfer(com.moneytransfer.dto.NewTransferDto,..)) && args(newTransferDto,..)")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction handleIdempotentTransferRequest(ProceedingJoinPoint proceedingJoinPoint, IdempotentTransferRequest idempotentTransferRequest, NewTransferDto newTransferDto) throws Throwable {
        return processRequest(newTransferDto, proceedingJoinPoint);
    }

    /**
     * Processes an idempotent transfer request.
     *
     * @param requestId
     * @param newTransferDto
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    private Transaction processRequest(final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
            var request = getOrSubmitRequest(newTransferDto);
            validateIdempotent(request, newTransferDto);
            return switch (request.getTransactionRequestStatus()) {
                case IN_PROGRESS -> processTransfer(newTransferDto, proceedingJoinPoint);
                case COMPLETED -> retrieveTransaction(request);
            };
    }

    /**
     * Gets or submits the request.
     *
     * @param requestId
     * @param newTransferDto
     * @return a submitted TransactionRequest
     * @throws MoneyTransferException
     */
    private TransactionRequest getOrSubmitRequest(final NewTransferDto newTransferDto) {
        Optional <TransactionRequest> optionalRequest = Optional.ofNullable(requestService.getRequest(newTransferDto.requestId()));
        return optionalRequest.orElseGet(()->requestService.submitRequest(newTransferDto));
    }

    /**
     * Processes the transfer and resolves the TransactionRequest.
     *
     * @param createTransactionRequest
     * @param proceedingJoinPoint
     * @return a new Transaction
     * @throws Throwable
     */
    private Transaction processTransfer(final NewTransferDto newTransferDto, final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Transaction transaction = null;
        HttpStatus httpStatus = null;
        String infoMessage = null;
        try {
            transaction = (Transaction) proceedingJoinPoint.proceed();
            httpStatus = GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS;
            infoMessage = "Transfer Request completed successfully.";
            return transaction;
        } catch (MoneyTransferException e) {
            httpStatus = e.getHttpStatus();
            infoMessage = e.getMessage();
            throw e;
        } finally {
            //throw new RuntimeException("Forcing rollback to test cache...");
            requestService.resolveRequest(newTransferDto, transaction, httpStatus, infoMessage);
        }
    }

    /**
     * Retrieves the Transaction of a TransactionRequest.
     *
     * @param transactionRequest
     * @return the persisted Transaction
     * @throws Throwable
     */
    private Transaction retrieveTransaction(final TransactionRequest transactionRequest) throws RequestConflictException {
        return Optional.ofNullable(transactionRequest.getTransaction())
                .orElseThrow(()->new RequestConflictException(transactionRequest.getInfoMessage(), transactionRequest.getHttpStatus()));
    }

    /**
     * Validates payload idempotency.
     *
     * @param transactionRequest
     * @param newTransferDto
     * @throws RequestConflictException
     */
    private void validateIdempotent(final TransactionRequest transactionRequest, final NewTransferDto newTransferDto) throws RequestConflictException {
        if (!areHashValuesEqual(newTransferDto, transactionRequest)) {
            var errorMessage = "The JSON body does not match with transactionRequest id " + transactionRequest.getRequestId() + ".";
            throw new RequestConflictException(errorMessage);
        }
    }

    /**
     * Compares the hashCodes of two NewTransferDto objects.
     * Returns true if the hashCodes match.
     * @param newTransferDto1
     * @param newTransferDto2
     * @return
     */
    private boolean areHashValuesEqual(NewTransferDto newTransferDto1, TransactionRequest transactionRequest){
		return newTransferDto1.hashCode() == transactionRequest.hashCode();
	}
}
