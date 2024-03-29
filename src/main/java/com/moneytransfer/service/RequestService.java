package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.util.UUID;

import org.springframework.http.HttpStatus;

/**
 * Service that submits, resolves and gets Requests.
 */
public interface RequestService {
    TransactionRequest getRequest(UUID requestId);

    TransactionRequest submitRequest(NewTransferDto newTransferDto);

    TransactionRequest resolveRequest(NewTransferDto newTransferDto, Transaction transaction, HttpStatus httpStatus, String infoMessage) throws MoneyTransferException;
}
