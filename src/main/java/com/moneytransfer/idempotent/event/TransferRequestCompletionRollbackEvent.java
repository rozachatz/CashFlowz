package com.moneytransfer.idempotent.event;

import com.moneytransfer.entity.Transfer;
import lombok.Getter;

@Getter
public class TransferRequestCompletionRollbackEvent extends IdempotentTransferRequestEvent {
    private final Transfer transfer;

    public TransferRequestCompletionRollbackEvent(Object source, Transfer transfer) {
        super(source);
        this.transfer = transfer;
    }
}
