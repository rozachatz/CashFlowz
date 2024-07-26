package com.moneytransfer.idempotent.event;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.TransferRequest;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
@Getter
public class NewTransferRequestEvent extends IdempotentTransferRequestEvent {
    private final NewTransferDto newTransferDto;
    private final CompletableFuture<TransferRequest> future;

    public NewTransferRequestEvent(Object source, NewTransferDto newTransferDto, CompletableFuture<TransferRequest> future) {
        super(source);
        this.newTransferDto = newTransferDto;
        this.future = future;
    }
}
