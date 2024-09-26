package com.cashflowz.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public class MoneyTransferException extends Exception implements HttpStatusProvider {

    public MoneyTransferException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}