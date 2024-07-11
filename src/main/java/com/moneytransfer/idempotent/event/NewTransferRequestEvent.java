package com.moneytransfer.idempotent.event;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.TransferRequest;

import java.util.concurrent.CompletableFuture;

public record NewTransferRequestEvent(NewTransferDto newTransferDto, CompletableFuture<TransferRequest> future) {
}
