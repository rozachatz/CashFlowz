package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidRequestStateException extends MoneyTransferException {
    public InvalidRequestStateException(UUID requestId) {
        super(String.format("Cannot process request [%s] due to invalid state.", requestId.toString()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

}
