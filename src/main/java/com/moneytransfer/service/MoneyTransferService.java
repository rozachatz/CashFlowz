package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.exceptions.MoneyTransferException;

/**
 * Service that performs the money transfer and persists the associated Transaction.
 */
public interface MoneyTransferService {
    /**
     * Performs a money transfer operation based on the specified concurrency control mode.
     * This method transfers money according to the specified concurrency control mode.
     *
     * @param newTransferDto         The data representing the transfer request.
     * @param concurrencyControlMode The concurrency control mode to use for the transfer operation.
     * @return The {@link Transaction} object representing the transfer.
     * @throws MoneyTransferException If an error occurs during the money transfer operation.
     */
    Transaction transfer(NewTransferDto newTransferDto, ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException;
}
