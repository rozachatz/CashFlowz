package com.moneytransfer.idempotent.event;

import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;
import lombok.Getter;

@Getter
public class TransferRequestCompletedWithSuccessEvent extends IdempotentTransferRequestEvent {
     private final TransferRequest transferRequest;
     private final Transfer transfer;

    public TransferRequestCompletedWithSuccessEvent(Object source, TransferRequest transferRequest, Transfer transfer) {
        super(source);
        this.transferRequest = transferRequest;
        this.transfer = transfer;
    }
}
