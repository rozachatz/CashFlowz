package com.moneytransfer.idempotent.event;

import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.exceptions.MoneyTransferException;

public record TransferRequestCompletionBusinessErrorEvent(TransferRequest transferRequest,
                                                          MoneyTransferException exception) {
}
