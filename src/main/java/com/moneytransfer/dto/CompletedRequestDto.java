package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * A Dto for completing a {@link com.moneytransfer.entity.TransactionRequest}.
 *
 * @param requestId
 * @param transaction
 * @param httpStatus
 * @param infoMessage
 * @see com.moneytransfer.service.RequestService#completeRequest(NewTransferDto, Transaction, HttpStatus, String)
 */
public record CompletedRequestDto(UUID requestId, Transaction transaction, HttpStatus httpStatus, String infoMessage) {
}
