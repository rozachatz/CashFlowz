package com.moneytransfer.idempotent.event;

import com.moneytransfer.entity.Transfer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransferRequestCompletionRollbackEvent {
    Transfer transfer;
}
