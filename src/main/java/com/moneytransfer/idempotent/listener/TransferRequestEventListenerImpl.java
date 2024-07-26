package com.moneytransfer.idempotent.listener;

import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.idempotent.event.NewTransferRequestEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletedWithErrorEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionRollbackEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletedWithSuccessEvent;
import com.moneytransfer.service.RefundMoneyService;
import com.moneytransfer.service.TransferRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Transactional
public class TransferRequestEventListenerImpl implements TransferRequestListener {
    private final TransferRequestService transferRequestService;
    private final RefundMoneyService refundMoneyService;

    @EventListener
    public void onNewTransferRequest(NewTransferRequestEvent event) {
        TransferRequest transferRequest = null;
        try {
            transferRequest = transferRequestService.getTransferRequest( event.getNewTransferDto().transferRequestId());
        } catch (ResourceNotFoundException e) {
            transferRequest = transferRequestService.createTransferRequest(event.getNewTransferDto());
        } finally {
            event.getFuture().complete(transferRequest);
        }
    }

    @EventListener
    public void onSuccessfulCompletion(TransferRequestCompletedWithSuccessEvent event) throws MoneyTransferException {
        transferRequestService.completeNewTransferRequestWithSuccess(event.getTransferRequest(), event.getTransfer());
    }

    @EventListener
    public void onCompletionWithError(TransferRequestCompletedWithErrorEvent event) throws MoneyTransferException {
        transferRequestService.completeNewTransferRequestWithError(event.getTransferRequest(), event.getException().getHttpStatus(), event.getException().getMessage());
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onCompletionRollback(TransferRequestCompletionRollbackEvent transferRequestCompletionRollbackEvent) throws MoneyTransferException {
        refundMoneyService.refundTransfer(transferRequestCompletionRollbackEvent.getTransfer());
    }

}