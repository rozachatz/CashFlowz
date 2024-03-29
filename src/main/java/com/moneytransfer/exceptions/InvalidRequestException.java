package com.moneytransfer.exceptions;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends MoneyTransferException {
    public InvalidRequestException(UUID requestId) {
        super("Cannot process the request with id: " +requestId+", it has inconsistent fields.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
