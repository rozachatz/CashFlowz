package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.exceptions.InsufficientRequestDataException;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.entity.Transfer;
import com.cashflowz.moneytransfer.entity.TransferRequest;
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
     * @param transferRequest The transfer request to complete.
     * @param httpStatus      The associated http status.
     * @param infoMessage     The associated exception message.
     * @return The completed {@link TransferRequest}.
     * @throws InsufficientRequestDataException if the given {@link TransferRequest} cannot be completed.
     */
    TransferRequest completeFailedTransferRequest(TransferRequest transferRequest, HttpStatus httpStatus, String infoMessage) throws InsufficientRequestDataException;

    /**
     * Completes a {@link TransferRequest} with success.
     *
     * @param transferRequest The transfer request to complete.
     * @param transfer        The associated {@link Transfer} object.
     * @return The completed {@link TransferRequest}.
     * @throws InsufficientRequestDataException if the given {@link TransferRequest} cannot be completed.
     */
    TransferRequest completeSuccessfulTransferRequest(TransferRequest transferRequest, Transfer transfer) throws InsufficientRequestDataException;

}
