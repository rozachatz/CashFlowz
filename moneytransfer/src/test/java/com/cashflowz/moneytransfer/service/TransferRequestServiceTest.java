package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.entity.Transfer;
import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.exceptions.InsufficientRequestDataException;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.repository.AccountRepository;
import com.cashflowz.moneytransfer.repository.TransferRepository;
import com.cashflowz.moneytransfer.service.TransferRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")

public class TransferRequestServiceTest {
    private final Transfer transfer;
    private final Account account1, account2;
    @Autowired
    private TransferRequestService transferRequestService;
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private AccountRepository accountRepository;

    public TransferRequestServiceTest() {
        account1 = new Account(0, UUID.randomUUID(), "testUsr", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        account2 = new Account(0, UUID.randomUUID(), "testUsr", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        transfer = new Transfer(UUID.randomUUID(), account1, account2, BigDecimal.ZERO, account1.getCurrency());

    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:10.5:///test_database_transfer_service");
    }

    @BeforeEach
    public void before() {
        accountRepository.saveAll(List.of(account1, account2));
        transferRepository.save(transfer);
    }

    @Test
    public void test_CreateAndGetTransferRequest() throws ResourceNotFoundException {
        var transferRequestId = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(transferRequestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        var transferRequestCreated = transferRequestService.createTransferRequest(newTransferDto);
        assertNotNull(transferRequestCreated);
        var transactionRequestRetrieved = transferRequestService.getTransferRequest(transferRequestId);
        assertNotNull(transactionRequestRetrieved);
        assertEquals(transferRequestCreated, transactionRequestRetrieved);
    }

    @Test
    public void test_CompleteFailedRequest() throws MoneyTransferException {
        var transferRequestId = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(transferRequestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        var transactionRequest = transferRequestService.createTransferRequest(newTransferDto);
        assertNotNull(transferRequestService.completeFailedTransferRequest(transactionRequest, HttpStatus.INTERNAL_SERVER_ERROR, "test"));
    }

    @Test
    public void test_CompleteSuccessfulRequest() throws MoneyTransferException {
        var transferRequestId = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(transferRequestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        var transactionRequest = transferRequestService.createTransferRequest(newTransferDto);
        assertNotNull(transferRequestService.completeSuccessfulTransferRequest(transactionRequest, transfer));
    }

    @Test
    public void test_CompleteSuccessfulRequest_InvalidData() {
        var transferRequestId = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(transferRequestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        var transactionRequest = transferRequestService.createTransferRequest(newTransferDto);
        assertThrows(InsufficientRequestDataException.class, () -> transferRequestService.completeSuccessfulTransferRequest(transactionRequest, null));
    }

    @Test
    public void test_CompleteFailedRequest_InvalidData() {
        var transferRequestId = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(transferRequestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        var transactionRequest = transferRequestService.createTransferRequest(newTransferDto);
        assertThrows(InsufficientRequestDataException.class, () -> transferRequestService.completeFailedTransferRequest(transactionRequest, null, "test"));
    }


}
