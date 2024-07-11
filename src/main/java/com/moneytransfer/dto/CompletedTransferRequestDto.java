package com.moneytransfer.dto;

import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * A Dto for completing a {@link TransferRequest}.
 *
 * @param transferRequestId
 * @param transfer
 * @param httpStatus
 * @param infoMessage
 */
public record CompletedTransferRequestDto(UUID transferRequestId, Transfer transfer, HttpStatus httpStatus,
                                          String infoMessage) {
}
