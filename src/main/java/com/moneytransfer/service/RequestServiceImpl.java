package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.ResolvedRequestDto;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.TransactionRequestStatus;
import com.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.moneytransfer.exceptions.InvalidRequestException;
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
    @Cacheable(cacheNames = "requestsCache", key = "#requestId")
    public TransactionRequest getRequest(final UUID requestId) {
        return transactionRequestRepository.findById(requestId).orElse(null);
    }

    @CachePut(cacheNames = "requestsCache", key = "#result.requestId")
    public TransactionRequest submitRequest(final NewTransferDto newTransferDto) {
        return transactionRequestRepository.save(new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS, null, null, null));
    }

    @CachePut(cacheNames = "requestsCache", key = "#result.requestId")
    public TransactionRequest resolveRequest(final NewTransferDto newTransferDto, final Transaction transaction, final HttpStatus httpStatus, String infoMessage) throws InvalidRequestException {
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.COMPLETED, httpStatus,  infoMessage, transaction);
        validateTransactionRequest(transactionRequest);
        transactionRequestRepository.resolveTransactionRequest(new ResolvedRequestDto(newTransferDto.requestId(), transaction, httpStatus, infoMessage));
        return transactionRequest;
    }

    private void validateTransactionRequest(TransactionRequest transactionRequest) throws InvalidRequestException {
        if (transactionRequest.getTransaction() == null && transactionRequest.getHttpStatus() == GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS || transactionRequest.getTransaction() != null && transactionRequest.getHttpStatus() != GlobalAPIExceptionHandler.SUCCESS_HTTP_STATUS){
            throw new InvalidRequestException(transactionRequest.getRequestId());
        }

    }

}
