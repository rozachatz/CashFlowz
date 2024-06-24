package com.moneytransfer.service;

import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetTransactionServiceTest {
    @InjectMocks
    GetTransactionServiceImpl getTransactionService;
    Account sourceAccount, targetAccount;
    List<Transaction> transactions;
    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    public void before() {
        sourceAccount = new Account(0, UUID.randomUUID(), "test", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), "test", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        Transaction transaction1 = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, BigDecimal.ZERO, sourceAccount.getCurrency());
        Transaction transaction2 = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, BigDecimal.ONE, sourceAccount.getCurrency());
        transactions = List.of(transaction1, transaction2);
    }

    @Test
    void testGetTransactions() {
        int maxRecords = 5;
        doReturn(new PageImpl<>(transactions)).when(transactionRepository).findAll(Pageable.ofSize(maxRecords));
        PageResponseDto<Transaction> result = getTransactionService.getTransactions(maxRecords);
        assertNotNull(result);
        assertEquals(transactions, result.content());
        verify(transactionRepository, times(1)).findAll(Pageable.ofSize(maxRecords));
    }

    @Test
    void testGetTransactionById_Success() throws ResourceNotFoundException {
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = transactions.get(0);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        Transaction result = getTransactionService.getTransactionById(transactionId);
        assertNotNull(result);
        assertEquals(transaction, result);
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    void testGetTransactionById_NotFound() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> getTransactionService.getTransactionById(transactionId));
        verify(transactionRepository, times(1)).findById(transactionId);
    }
}
