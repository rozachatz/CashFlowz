package com.cashflowz.moneytransfer.exceptions;

import org.springframework.http.HttpStatus;

public interface HttpStatusProvider {
    HttpStatus getHttpStatus();
}
