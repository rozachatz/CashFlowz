package com.moneytransfer.service;

import com.moneytransfer.dto.CompletedTransferRequestDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.enums.TransferRequestStatus;
import com.moneytransfer.exceptions.InsufficientRequestDataException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLTransientConnectionException;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link TransferRequestService}
 */
@Component
@RequiredArgsConstructor
@Transactional
public class TransferRequestServiceImpl implements TransferRequestService {
    private final TransferRequestRepository transferRequestRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "moneyTransferRequestsCache")
    public TransferRequest getTransferRequest(final UUID requestId) throws ResourceNotFoundException {
        return transferRequestRepository.findById(requestId).orElseThrow(() -> new ResourceNotFoundException(TransferRequest.class, requestId));
    }

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.transferRequestId")
    public TransferRequest createTransferRequest(final NewTransferDto newTransferDto) {
        return transferRequestRepository.save(new TransferRequest(newTransferDto.transferRequestId(), newTransferDto.amount(),
                newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), com.moneytransfer.enums.TransferRequestStatus.IN_PROGRESS,
                null, null, null));
    }

    @Retryable(retryFor = SQLTransientConnectionException.class, backoff = @Backoff(delay = 3000))
    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.transferRequestId")
    public TransferRequest completeFailedTransferRequest(TransferRequest transferRequest, HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException {
        validateCompletionWithError(transferRequest.getTransferRequestId(), httpStatus, infoMessage);
        return completeTransferRequest(new TransferRequest(transferRequest.getTransferRequestId(), transferRequest.getAmount(), transferRequest.getSourceAccountId(), transferRequest.getTargetAccountId(), TransferRequestStatus.COMPLETED, httpStatus, infoMessage, null));
    }

    @Retryable(retryFor = SQLTransientConnectionException.class, backoff = @Backoff(delay = 3000))
    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.transferRequestId")
    public TransferRequest completeSuccessfulTransferRequest(final TransferRequest transferRequest, final Transfer transfer) throws InsufficientRequestDataException {
        validateCompletionWithSuccess(transferRequest.getTransferRequestId(), transfer);
        return completeTransferRequest(new TransferRequest(transferRequest.getTransferRequestId(), transferRequest.getAmount(), transferRequest.getSourceAccountId(), transferRequest.getTargetAccountId(), TransferRequestStatus.COMPLETED, HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase(), transfer));
    }

    /**
     * Validates that the {@link TransferRequest} with the given id can be completed as successful.
     * A successful {@link TransferRequest} is always associated with a non-empty {@link Transfer}.
     *
     * @param newTransactionRequestId The id of the {@link TransferRequest}.
     * @param transfer                The associated {@link Transfer} object.
     * @throws InsufficientRequestDataException
     */
    private void validateCompletionWithSuccess(UUID newTransactionRequestId, Transfer transfer) throws InsufficientRequestDataException {
        Optional.ofNullable(transfer).orElseThrow(() -> new InsufficientRequestDataException(newTransactionRequestId));
    }

    /**
     * Validates that the {@link TransferRequest} with the given id can be completed as failed.
     * For a failed {@link TransferRequest}, the associated {@link HttpStatus}
     * and exception message should be provided.
     *
     * @param newTransactionRequestId
     * @param httpStatus
     * @param infoMessage
     * @throws InsufficientRequestDataException
     */
    private void validateCompletionWithError(UUID newTransactionRequestId, HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException {
        if (httpStatus == null || infoMessage == null)
            throw new InsufficientRequestDataException(newTransactionRequestId);
    }

    /**
     * Completes the given {@link TransferRequest} by updating the associated entity.
     *
     * @param completedRequest The {@link TransferRequest} for completion.
     * @return
     */
    private TransferRequest completeTransferRequest(TransferRequest completedRequest) {
        transferRequestRepository.completeRequest(new CompletedTransferRequestDto(completedRequest.getTransferRequestId(), completedRequest.getTransfer(), completedRequest.getHttpStatus(), completedRequest.getInfoMessage()));
        return completedRequest;
    }
}
