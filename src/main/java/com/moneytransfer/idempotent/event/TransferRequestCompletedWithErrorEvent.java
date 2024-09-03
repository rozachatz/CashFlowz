package com.moneytransfer.idempotent.event;

import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import lombok.Getter;

@Getter
public class TransferRequestCompletedWithErrorEvent extends IdempotentTransferRequestEvent {
    private final TransferRequest transferRequest;
    private final MoneyTransferException exception;

    public TransferRequestCompletedWithErrorEvent(Object source, TransferRequest transferRequest, MoneyTransferException exception) {
        super(source);
        this.transferRequest = transferRequest;
        this.exception = exception;
    }
}
