package com.cashflowz.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InsufficientRequestDataException extends MoneyTransferException {
    public InsufficientRequestDataException(UUID requestId) {
        super(String.format("Insufficient or invalid data for request [%s].", requestId.toString()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
