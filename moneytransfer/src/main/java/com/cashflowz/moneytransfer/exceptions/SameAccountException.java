package com.cashflowz.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class SameAccountException extends MoneyTransferException {
    public SameAccountException(UUID accountId) {
        super(String.format("Transfers in the same Account [%s] is not allowed.", accountId));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
