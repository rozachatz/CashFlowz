package com.moneytransfer.idempotent.listener;

import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.idempotent.event.NewTransferRequestEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletedWithErrorEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionRollbackEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletedWithSuccessEvent;

public interface TransferRequestListener {
    void onNewTransferRequest(NewTransferRequestEvent event);
    void onSuccessfulCompletion(TransferRequestCompletedWithSuccessEvent event) throws MoneyTransferException;
    void onCompletionWithError(TransferRequestCompletedWithErrorEvent event) throws MoneyTransferException;
    void onCompletionRollback(TransferRequestCompletionRollbackEvent transferRequestCompletionRollbackEvent) throws MoneyTransferException;
}
