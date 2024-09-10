package com.moneytransfer.dto;

import com.moneytransfer.exceptions.MoneyTransferException;

/**
 * Dto for exception handling.
 *
 * @param reasonPhrase     The reasonPhrase of {@link org.springframework.http.HttpStatus}.
 * @param exceptionMessage The exception message.
 * @see com.moneytransfer.exceptions.GlobalAPIExceptionHandler#handleMoneyExceptions(MoneyTransferException)
 */
public record ErrorResponseDto(String reasonPhrase, String exceptionMessage) {
}