package com.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidEntityStateException extends MoneyTransferException {
    public InvalidEntityStateException(Class<?> resourceClass, UUID requestId) {
        super(String.format("Cannot process entity $1%s with id [$2%s] due to invalid state.", resourceClass.getName(), requestId.toString()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

}
