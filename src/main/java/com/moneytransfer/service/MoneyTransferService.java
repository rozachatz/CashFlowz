package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.LockControlMode;
import com.moneytransfer.exceptions.MoneyTransferException;

/**
 * Service that performs the money transfer and persists the associated Transaction.
 */
public interface MoneyTransferService {
    /**
     * Performs a money transfer operation based on the specified lock control mode.
     *
     * @param newTransferDto  The data representing the transfer request.
     * @param lockControlMode The lock control mode to use for the transfer operation.
     * @return The {@link Transaction} object representing the transfer.
     * @throws MoneyTransferException If an error occurs during the money transfer operation.
     */
    Transaction transferWithLocking(NewTransferDto newTransferDto, LockControlMode lockControlMode) throws MoneyTransferException;

    /**
     * Performs a serializable money transfer operation.
     *
     * @param newTransferDto  The data representing the transfer request.
     * @return The {@link Transaction} object representing the transfer.
     * @throws MoneyTransferException If an error occurs during the money transfer operation.
     */
    Transaction transferSerializable(NewTransferDto newTransferDto) throws MoneyTransferException;
}
