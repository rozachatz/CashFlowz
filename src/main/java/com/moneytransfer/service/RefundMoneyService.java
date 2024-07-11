package com.moneytransfer.service;

import com.moneytransfer.entity.Transfer;
import com.moneytransfer.exceptions.MoneyTransferException;

public interface RefundMoneyService {
    void refundTransfer(Transfer transfer) throws MoneyTransferException;
}
