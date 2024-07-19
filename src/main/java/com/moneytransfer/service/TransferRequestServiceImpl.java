package com.moneytransfer.service;

import com.moneytransfer.dto.CompletedTransferRequestDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.enums.TransferRequestStatus;
import com.moneytransfer.enums.TransferStatus;
import com.moneytransfer.exceptions.InsufficientRequestDataException;
import com.moneytransfer.exceptions.InvalidEntityStateException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.transferRequestId")
    public TransferRequest completeNewTransferRequestWithSuccess(final TransferRequest transferRequest, final Transfer transfer) throws InsufficientRequestDataException, InvalidEntityStateException {
        validateCompletionWithSuccess(transferRequest.getTransferRequestId(), transfer);
        var completedRequest = new TransferRequest(transferRequest.getTransferRequestId(), transferRequest.getAmount(), transferRequest.getSourceAccountId(), transferRequest.getTargetAccountId(), com.moneytransfer.enums.TransferRequestStatus.COMPLETED, HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase(), transfer);
        var newCompletedRequestDto = new CompletedTransferRequestDto(completedRequest.getTransferRequestId(), completedRequest.getTransfer(), HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase());
        transferRequestRepository.completeRequest(newCompletedRequestDto);
        return completedRequest;
    }

    @CachePut(cacheNames = "moneyTransferRequestsCache", key = "#result.transferRequestId")
    public TransferRequest completeNewTransferRequestWithError(TransferRequest transferRequest, HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException {
        validateCompletionWithError(transferRequest.getTransferRequestId(), httpStatus, infoMessage);
        var completedRequest = new TransferRequest(transferRequest.getTransferRequestId(), transferRequest.getAmount(), transferRequest.getSourceAccountId(), transferRequest.getTargetAccountId(), TransferRequestStatus.COMPLETED, httpStatus, infoMessage, null);
        var newCompletedRequestDto = new CompletedTransferRequestDto(transferRequest.getTransferRequestId(), transferRequest.getTransfer(), httpStatus, infoMessage);
        transferRequestRepository.completeRequest(newCompletedRequestDto);
        return completedRequest;
    }

    private void validateCompletionWithSuccess(UUID newTransactionRequestId, Transfer transfer) throws InsufficientRequestDataException, InvalidEntityStateException {
        Optional.ofNullable(transfer).orElseThrow(() -> new InsufficientRequestDataException(newTransactionRequestId));
        if (!transfer.getTransferStatus().equals(TransferStatus.FUNDS_TRANSFERRED)) {
            throw new InvalidEntityStateException(Transfer.class, transfer.getTransferId());
        }
    }

    private void validateCompletionWithError(UUID newTransactionRequestId, HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException {
        if (httpStatus == null || infoMessage == null)
            throw new InsufficientRequestDataException(newTransactionRequestId);
    }
}
