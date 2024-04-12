package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Service that submits, resolves and gets Requests.
 */
public interface RequestService {
    /**
     * Gets a {@link TransactionRequest} by requestId.
     * <p>
     * Note: This method returns null if the TransactionRequest is not found, it should be used with caution.
     * </p>
     *
     * @param requestId
     * @return a TransactionRequest
     */
    TransactionRequest getRequest(UUID requestId);

    /**
     * Creates a new {@link TransactionRequest}.
     *
     * @param newTransferDto contains the fields for the new {@link TransactionRequest} object.
     * @return
     */
    TransactionRequest createRequest(NewTransferDto newTransferDto);

    /**
     * Completes a {@link TransactionRequest}.
     *
     * @param newTransferDto
     * @param transaction
     * @param httpStatus
     * @param infoMessage
     * @return a TransactionRequest with status {@link com.moneytransfer.enums.TransactionRequestStatus#COMPLETED}.
     * @throws MoneyTransferException
     */
    TransactionRequest completeRequest(NewTransferDto newTransferDto, Transaction transaction, HttpStatus httpStatus, String infoMessage) throws MoneyTransferException;
}
