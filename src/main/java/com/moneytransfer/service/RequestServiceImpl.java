package com.moneytransfer.service;

import com.moneytransfer.dto.CompletedRequestDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.TransactionRequestStatus;
import com.moneytransfer.exceptions.InsufficientRequestDataException;
import com.moneytransfer.exceptions.InvalidRequestStateException;
import com.moneytransfer.repository.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link RequestService}
 */
@Component
@RequiredArgsConstructor
@Transactional
class RequestServiceImpl implements RequestService {
    private final TransactionRequestRepository transactionRequestRepository;

    @Cacheable(cacheNames = "moneyTransferRequestsCache")
    public TransactionRequest getRequest(final UUID requestId) {
        return transactionRequestRepository.findById(requestId).orElse(null);
    }

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.requestId")
    public TransactionRequest createRequest(final NewTransferDto newTransferDto) {
        return transactionRequestRepository.save(new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(),
                newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS,
                null, null, null));
    }

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.requestId")
    public TransactionRequest completeRequest(final NewTransferDto newTransferDto, final Transaction transaction, final HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException, InvalidRequestStateException {
        validateInProgress(newTransferDto);
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.COMPLETED, httpStatus, infoMessage, transaction);
        validateCompletion(transactionRequest);
        transactionRequestRepository.completeRequest(new CompletedRequestDto(newTransferDto.requestId(), transaction, httpStatus, infoMessage));
        return transactionRequest;
    }

    /**
     * Validates that the persisted request is valid for completion.
     * This method ensures that the transaction request exists, is in progress, has null transaction and informationMessage fields,
     * and matches the provided {@link NewTransferDto}.
     *
     * @param newTransferDto the new transfer data transfer object to validate against
     * @throws InvalidRequestStateException if the request is not in progress, if the transaction info is not null,
     *                                      or if the {@link NewTransferDto} does not match the transaction request
     */
    private void validateInProgress(NewTransferDto newTransferDto) throws InvalidRequestStateException {
        TransactionRequest transactionRequest = transactionRequestRepository.findById(newTransferDto.requestId()).orElseThrow(() -> new InvalidRequestStateException(newTransferDto.requestId()));
        if (transactionRequest.getTransactionRequestStatus() != TransactionRequestStatus.IN_PROGRESS || !isNullTransactionAndInfoMsg(transactionRequest) || !transactionRequest.toNewTransferDto().equals(newTransferDto)) {
            throw new InvalidRequestStateException(transactionRequest.getRequestId());
        }
    }

    /**
     * Checks if the transaction and infoMessage and information in the {@link TransactionRequest} are null.
     * <p>A request with {@link TransactionRequestStatus#IN_PROGRESS} should have these fields set to null.</p>
     *
     * @param transactionRequest the transaction request to check
     * @return {@link Boolean#TRUE} if the transaction and info message are both null, {@link Boolean#FALSE} otherwise
     */
    private boolean isNullTransactionAndInfoMsg(TransactionRequest transactionRequest) {
        return transactionRequest.getTransaction() == null && transactionRequest.getInfoMessage() == null;
    }

    /**
     * Validates that the request data in {@link TransactionRequest} comply with the business rules for changing
     * the status to {@link TransactionRequestStatus#COMPLETED}.
     * <p>If the request data is insufficient or invalid, an {@link InsufficientRequestDataException} is thrown.</p>
     *
     * @param transactionRequest the {@link TransactionRequest} to validate.
     * @throws InsufficientRequestDataException if the request data is insufficient or invalid.
     */
    private void validateCompletion(TransactionRequest transactionRequest) throws InsufficientRequestDataException {
        if (isNullInfoMsgAndStatus(transactionRequest) || !isTransactionStatusPairValid(transactionRequest)) {
            throw new InsufficientRequestDataException(transactionRequest.getRequestId());
        }
    }

    /**
     * Checks if the info message and HTTP status in the {@link TransactionRequest} are null.
     * A completed request should not have null values for info message and/or HTTP status.
     *
     * @param transactionRequest the transaction request to check
     * @return {@link Boolean#TRUE} if either the info message or HTTP status is null, {@link Boolean#FALSE} otherwise.
     */
    private boolean isNullInfoMsgAndStatus(TransactionRequest transactionRequest) {
        return (transactionRequest.getInfoMessage() == null || transactionRequest.getHttpStatus() == null);
    }

    /**
     * Validates the transaction and HTTP status pair in the {@link TransactionRequest}.
     * The rules are:
     * - If the transaction is null, the HTTP status must not indicate success.
     * - If the transaction is not null, the HTTP status must indicate success.
     *
     * @param transactionRequest the transaction request to validate
     * @return {@link Boolean#TRUE} if the transaction and HTTP status pair are valid, {@link Boolean#FALSE} otherwise.
     */
    private boolean isTransactionStatusPairValid(TransactionRequest transactionRequest) {
        return (transactionRequest.getTransaction() == null && !transactionRequest.getHttpStatus().is2xxSuccessful())
                || (transactionRequest.getTransaction() != null && transactionRequest.getHttpStatus().is2xxSuccessful());
    }

}
