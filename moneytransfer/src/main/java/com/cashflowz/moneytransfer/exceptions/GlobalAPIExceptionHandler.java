package com.cashflowz.moneytransfer.exceptions;

import com.cashflowz.moneytransfer.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalAPIExceptionHandler {

    @ExceptionHandler(MoneyTransferException.class)
    public ResponseEntity<ErrorResponseDto> handleMoneyExceptions(MoneyTransferException e) {
        log.error(e.getMessage(), e);
        HttpStatus status = e.getHttpStatus();
        return ResponseEntity.status(status).body(new ErrorResponseDto(status.getReasonPhrase(), e.getMessage()));
    }

}
