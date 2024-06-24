package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientRequestDataException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
public class RequestServiceTest {
    @Autowired
    private RequestService requestService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    Transaction transaction;

    @BeforeEach
    public void before(){
        Account account1 = new Account(0,UUID.randomUUID(),"testUsr", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        Account account2 = new Account(0,UUID.randomUUID(),"testUsr", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(account1, account2));
        transaction = new Transaction(UUID.randomUUID(), account1, account2, BigDecimal.ZERO, account1.getCurrency());
        transactionRepository.save(transaction);
    }

    @Test
    public void test_CreateAndGetRequest() throws InsufficientRequestDataException {
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        requestService.createRequest(newTransferDto);
        Assertions.assertNotNull(requestService.getRequest(requestId));
    }

    @Test
    public void test_CompleteFailedRequest() throws MoneyTransferException {
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        TransactionRequest transactionRequest = requestService.createRequest(newTransferDto);
        requestService.completeRequest(transactionRequest, null, HttpStatus.INTERNAL_SERVER_ERROR, "test");
    }

    @Test
    public void test_CompleteFailedRequest_InvalidData() throws InsufficientRequestDataException {
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        TransactionRequest transactionRequest = requestService.createRequest(newTransferDto);
        assertThrows(InsufficientRequestDataException.class, ()->requestService.completeRequest(transactionRequest, transaction, HttpStatus.INTERNAL_SERVER_ERROR, "test"));
    }

    @Test
    public void test_CompleteSuccessfulRequest() throws MoneyTransferException {
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        TransactionRequest transactionRequest = requestService.createRequest(newTransferDto);
        requestService.completeRequest(transactionRequest, transaction, HttpStatus.CREATED, "test");
    }

    @Test
    public void test_CompleteSuccessfulRequest_InvalidData() throws MoneyTransferException {
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        TransactionRequest transactionRequest = requestService.createRequest(newTransferDto);
        assertThrows(InsufficientRequestDataException.class, ()->requestService.completeRequest(transactionRequest, null, HttpStatus.CREATED, "test"));
    }
}
