package com.moneytransfer.idempotent.event;

import com.moneytransfer.entity.Transfer;
import com.moneytransfer.entity.TransferRequest;

public record TransferRequestCompletionSuccessEvent(TransferRequest transferRequest, Transfer transfer) {
}
