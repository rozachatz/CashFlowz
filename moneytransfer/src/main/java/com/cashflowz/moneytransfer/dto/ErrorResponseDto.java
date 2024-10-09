package com.cashflowz.moneytransfer.dto;

import com.cashflowz.moneytransfer.exceptions.GlobalAPIExceptionHandler;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;

/**
 * Dto for exception handling.
 *
 * @param reasonPhrase     The reasonPhrase of {@link org.springframework.http.HttpStatus}.
 * @param exceptionMessage The exception message.
 * @see GlobalAPIExceptionHandler#handleMoneyExceptions(MoneyTransferException)
 */
public record ErrorResponseDto(String reasonPhrase, String exceptionMessage) {
}