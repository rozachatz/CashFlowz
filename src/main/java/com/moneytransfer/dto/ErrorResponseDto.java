package com.moneytransfer.dto;

/**
 * Dto for logging and exception handling
 * Retains HttpStatus reasonPhrase and a detailed information message.
 */
public record ErrorResponseDto (String reasonPhrase, String infoMessage){
}