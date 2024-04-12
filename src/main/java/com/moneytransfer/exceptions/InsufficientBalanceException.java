package com.moneytransfer.exceptions;

import com.moneytransfer.entity.Account;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class InsufficientBalanceException extends MoneyTransferException {
    public InsufficientBalanceException(Account account, BigDecimal amount) {
        super(String.format("Insufficient source account balance [%s]. Requested Amount: %s, Available Balance: %s",
                account.getAccountId(), amount, account.getBalance()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.PAYMENT_REQUIRED;
    }
}
