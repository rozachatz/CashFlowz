package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public record ResolvedRequestDto(UUID requestId, Transaction transaction, HttpStatus httpStatus, String infoMessage){
}
