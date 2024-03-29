package com.moneytransfer.service;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for {@link GetTransactionService}.
 */
@Service
@RequiredArgsConstructor
class GetTransactionServiceImpl implements GetTransactionService {
    /**
     * The transaction repository.
     */
    private final TransactionRepository transactionRepository;

    /**
     * Gets all transactions with limited maximum number of results.
     *
     * @param maxRecords
     * @return Accounts
     */
    public Page<Transaction> getTransactions(final int maxRecords) {
        var pageRequest = PageRequest.of(0, maxRecords);
        return transactionRepository.findAll(pageRequest);
    }

    /**
     * Gets {@link Transaction} by id.
     *
     * @param transactionId
     * @return Transaction
     * @throws ResourceNotFoundException
     */
    public Transaction getTransactionById(final UUID transactionId) throws ResourceNotFoundException {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(transactionId));
    }

}
