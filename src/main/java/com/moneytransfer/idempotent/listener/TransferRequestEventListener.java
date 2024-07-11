package com.moneytransfer.idempotent.listener;

import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.idempotent.event.NewTransferRequestEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionBusinessErrorEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionRollbackEvent;
import com.moneytransfer.idempotent.event.TransferRequestCompletionSuccessEvent;
import com.moneytransfer.service.RefundMoneyService;
import com.moneytransfer.service.TransferRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class TransferRequestEventListener {
    private final TransferRequestService transferRequestService;
    private final RefundMoneyService refundMoneyService;

    @EventListener
    public void handleNewTransferRequest(NewTransferRequestEvent event) {
        var transactionRequest = transferRequestService.getTransferRequest(event.newTransferDto().transferRequestId());
        TransferRequest transferRequest = Optional.ofNullable(transactionRequest).orElseGet(() -> transferRequestService.createTransferRequest(event.newTransferDto()));
        event.future().complete(transferRequest);
    }

    @EventListener
    public void handleTransferRequestSuccessfulCompletion(TransferRequestCompletionSuccessEvent event) throws MoneyTransferException {
        transferRequestService.completeNewTransferRequestWithSuccess(event.transferRequest(), event.transfer());
    }

    @EventListener
    public void handleTransferRequestCompletionWithBusinessError(TransferRequestCompletionBusinessErrorEvent event) throws MoneyTransferException {
        transferRequestService.completeNewTransferRequestWithError(event.transferRequest(), event.exception().getHttpStatus(), event.exception().getMessage());
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleTransferRequestCompletionWithRollback(TransferRequestCompletionRollbackEvent transferRequestCompletionRollbackEvent) throws MoneyTransferException {
        refundMoneyService.refundTransfer(transferRequestCompletionRollbackEvent.getTransfer());
    }

}