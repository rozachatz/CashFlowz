package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Service that submits, resolves and gets Requests.
 */
public interface TransferRequestService {
    /**
     * Gets a {@link TransferRequest} by transferRequestId.
     * <p>
     * Note: This method returns null if the TransferRequest is not found, it should be used with caution.
     * </p>
     *
     * @param transferRequestId
     * @return a TransferRequest
     */
    TransferRequest getTransferRequest(UUID transferRequestId) throws ResourceNotFoundException;

    /**
     * Creates a new {@link TransferRequest}.
     *
     * @param newTransferDto contains the fields for the new {@link TransferRequest} object.
     * @return
     */
    TransferRequest createTransferRequest(NewTransferDto newTransferDto);

    /**
     * Completes a {@link TransferRequest} with  business error.
     *
     * @param transferRequest
     * @param httpStatus
     * @param infoMessage
     * @return
     * @throws MoneyTransferException
     */
    TransferRequest completeNewTransferRequestWithError(TransferRequest transferRequest, HttpStatus httpStatus, String infoMessage) throws MoneyTransferException;

    /**
     * Completes a {@link TransferRequest} with success.
     *
     * @param transferRequest
     * @param transfer
     * @return
     * @throws MoneyTransferException
     */
    TransferRequest completeNewTransferRequestWithSuccess(TransferRequest transferRequest, Transfer transfer) throws MoneyTransferException;

}
