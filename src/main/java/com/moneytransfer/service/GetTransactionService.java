package com.moneytransfer.service;

import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service that gets {@link Transaction} entities.
 */
public interface GetTransactionService {
    /**
     * Gets a transaction by id.
     *
     * @param transactionId
     * @return {@link Transaction}
     * @throws ResourceNotFoundException if no {@link Transaction} was found for this id.
     */
    Transaction getTransactionById(UUID transactionId) throws ResourceNotFoundException;

    /**
     * Gets {@link Transaction} records.
     *
     * @param maxRecords The maximum number of records that will be returned.
     * @return PageResponseDto for the transactions.
     */
    PageResponseDto<Transaction> getTransactions(int maxRecords);

}
