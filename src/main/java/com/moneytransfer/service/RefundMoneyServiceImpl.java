package com.moneytransfer.service;

import com.moneytransfer.entity.Transfer;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@RequiredArgsConstructor
@Service
public class RefundMoneyServiceImpl implements RefundMoneyService {

    private final TransferRepository transferRepository;

    private final CurrencyExchangeService currencyExchangeService;

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = REQUIRES_NEW)
    public void refundTransfer(Transfer transfer) throws MoneyTransferException {
        transferRepository.findById(transfer.getTransferId()).orElseThrow(() -> new ResourceNotFoundException(Transfer.class, transfer.getTransferId()));
        updateBalances(transfer);
        transferRepository.updateRefundedTransfer(transfer.getTransferId());
    }

    private void updateBalances(Transfer transfer) throws MoneyTransferException {
        transfer.getSourceAccount().credit(transfer.getAmount());
        var targetAmount = transfer.getAmount();
        if (!transfer.getSourceAccount().getCurrency().equals(transfer.getTargetAccount().getCurrency())) {
            targetAmount = currencyExchangeService.exchange(transfer.getAmount(), transfer.getSourceAccount().getCurrency(), transfer.getTargetAccount().getCurrency());
        }
        transfer.getTargetAccount().debit(targetAmount);
    }

}
