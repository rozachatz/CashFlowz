package com.moneytransfer.service;

import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation for {@link GetTransactionService}.
 */
@Service
@RequiredArgsConstructor
class GetTransactionServiceImpl implements GetTransactionService {
    private final TransactionRepository transactionRepository;

    public PageResponseDto<Transaction> getTransactions(final int maxRecords) {
        var pageRequest = PageRequest.of(0, maxRecords);
        List<Transaction> transactions = transactionRepository.findAll(pageRequest).toList();
        return new PageResponseDto<>(transactions);
    }

    public Transaction getTransactionById(final UUID transactionId) throws ResourceNotFoundException {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(Transaction.class, transactionId));
    }

}
