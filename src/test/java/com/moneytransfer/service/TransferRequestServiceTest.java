package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransferStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransferRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        this.account1 = new Account(0, UUID.randomUUID(), "testUsr", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        this.account2 = new Account(0, UUID.randomUUID(), "testUsr", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        this.transfer = new Transfer(UUID.randomUUID(), account1, account2, BigDecimal.ZERO, account1.getCurrency(), TransferStatus.FUNDS_TRANSFERRED);

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
        transferRequestService.completeNewTransferRequestWithError(transactionRequest, HttpStatus.INTERNAL_SERVER_ERROR, "test");
    }

    @Test
    public void test_CompleteSuccessfulRequest() throws MoneyTransferException {
        var transferRequestId = UUID.randomUUID();
        var newTransferDto = new NewTransferDto(transferRequestId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        var transactionRequest = transferRequestService.createTransferRequest(newTransferDto);
        transferRequestService.completeNewTransferRequestWithSuccess(transactionRequest, transfer);
    }

}
