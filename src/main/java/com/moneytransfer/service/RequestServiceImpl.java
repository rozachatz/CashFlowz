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
public class RequestServiceImpl implements RequestService {
    private final TransactionRequestRepository transactionRequestRepository;

    @Cacheable(cacheNames = "moneyTransferRequestsCache")
    public TransactionRequest getRequest(final UUID requestId) {
        return transactionRequestRepository.findById(requestId).orElse(null);
    }

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.requestId")
    public TransactionRequest createRequest(final NewTransferDto newTransferDto) throws InsufficientRequestDataException {
        return transactionRequestRepository.save(new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(),
                newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS,
                null, null, null));
    }

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.requestId")
    public TransactionRequest completeRequest(final TransactionRequest transactionRequest, final Transaction transaction, final HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException, InvalidRequestStateException {
        validateRequestState(transactionRequest.getRequestId());
        var completedRequest = new TransactionRequest(transactionRequest.getRequestId(), transactionRequest.getAmount(), transactionRequest.getSourceAccountId(), transactionRequest.getTargetAccountId(), TransactionRequestStatus.COMPLETED, httpStatus, infoMessage, transaction);
        validateRequestForCompletion(completedRequest);
        transactionRequestRepository.completeRequest(new CompletedRequestDto(transactionRequest.getRequestId(), transaction, httpStatus, infoMessage));
        return completedRequest;
    }

    public void validateRequestForCompletion(TransactionRequest transactionRequest) throws InsufficientRequestDataException, InvalidRequestStateException {
        if (!areFieldsValidForCompletion(transactionRequest)) {
            throw new InsufficientRequestDataException(transactionRequest.getRequestId());
        }
    }

    public void validateRequestState(UUID requestId) throws InvalidRequestStateException {
        transactionRequestRepository.findById(requestId)
                .filter(t -> t.getTransactionRequestStatus() == TransactionRequestStatus.IN_PROGRESS)
                .orElseThrow(() -> new InvalidRequestStateException(requestId));
    }

    private boolean areFieldsValidForCompletion(TransactionRequest transactionRequest) {
        if (transactionRequest.getInfoMessage() == null) return false;
        if (transactionRequest.getHttpStatus().is2xxSuccessful()) {
            return transactionRequest.getTransaction() != null;
        }
        return transactionRequest.getTransaction() == null;
    }

}
