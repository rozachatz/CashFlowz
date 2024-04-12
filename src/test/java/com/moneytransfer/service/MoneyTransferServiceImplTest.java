/**
 * Test class for {@link com.moneytransfer.service.MoneyTransferServiceImpl}
 * This test uses embedded h2 db.
 */
package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionRequestStatus;
import com.moneytransfer.exceptions.*;
import com.moneytransfer.repository.AccountRepository;
import com.moneytransfer.repository.TransactionRepository;
import com.moneytransfer.repository.TransactionRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
public class MoneyTransferServiceImplTest {
    /**
     * CurrencyExchange Service
     */
    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    /**
     * MoneyTransfer Service
     */
    @Autowired
    private MoneyTransferServiceImpl moneyTransferService;
    /**
     * Transaction Service
     */
    @Autowired
    private GetAccountServiceImpl accountService;

    /**
     * Account repository
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * Transaction repository
     */
    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * TransactionRequest repository
     */
    @Autowired
    private TransactionRequestRepository transactionRequestRepository;


    /**
     * Source Account
     */
    private Account sourceAccount;
    /**
     * Target Account
     */
    private Account targetAccount;
    /**
     * Target Account1
     */
    private Account targetAccount1;

    /**
     * Insert new accounts for each test.
     */
    @BeforeEach
    public void setup() {
        BigDecimal balance = BigDecimal.valueOf(10);
        sourceAccount = new Account(0, UUID.randomUUID(), "Name1", balance, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), "Name2", balance, Currency.USD, LocalDateTime.now());
        targetAccount1 = new Account(0, UUID.randomUUID(), "Name3", balance, Currency.EUR, LocalDateTime.now());
        accountRepository.saveAll(List.of(targetAccount, targetAccount1, sourceAccount));
    }

    /**
     * Test for happy path.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_HappyPath() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccount1.getBalance().add(amount);
        moneyTransferService.transfer(new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount1.getAccountId(), amount), ConcurrencyControlMode.OPTIMISTIC_LOCKING);
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var actualTargetBalance = retrievePersistedBalance(targetAccount1.getAccountId());
        expectedTargetBalance = expectedTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        actualTargetBalance = actualTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        assertEquals(actualTargetBalance, expectedTargetBalance);
        validateResolvedRequest(requestId, HttpStatus.CREATED);
    }

    /**
     * Test for happy path with currency exchange.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testExchange_HappyPath() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccount.getBalance().add(currencyExchangeService.exchange(amount, sourceAccount.getCurrency(), targetAccount.getCurrency()));
        moneyTransferService.transfer(new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount), ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var actualTargetBalance = retrievePersistedBalance(targetAccount.getAccountId());
        expectedTargetBalance = expectedTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        actualTargetBalance = actualTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        assertEquals(actualTargetBalance, expectedTargetBalance);
        validateResolvedRequest(requestId, HttpStatus.CREATED);
    }

    /**
     * Test for insufficient balance.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_InsufficientBalance() throws MoneyTransferException {
        var amount = sourceAccount.getBalance().multiply(BigDecimal.valueOf(10));
        var requestId = UUID.randomUUID();
        assertThrows(InsufficientBalanceException.class, () -> moneyTransferService.transfer(new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount), ConcurrencyControlMode.PESSIMISTIC_LOCKING));
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        var expectedSourceBalance = sourceAccount.getBalance();
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var expectedTargetBalance = targetAccount.getBalance();
        var actualTargetBalance = retrievePersistedBalance(targetAccount.getAccountId()).stripTrailingZeros();
        assertEquals(actualTargetBalance.stripTrailingZeros(), expectedTargetBalance.stripTrailingZeros());
        validateResolvedRequest(requestId, HttpStatus.PAYMENT_REQUIRED);
    }

    /**
     * Test for same account.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_TransferSameAccount() throws MoneyTransferException {
        var amount = BigDecimal.ONE;
        var expectedBalance = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        assertThrows(SameAccountException.class, () -> moneyTransferService.transfer(new NewTransferDto(requestId, sourceAccount.getAccountId(), sourceAccount.getAccountId(), amount), ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        var actualBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualBalance.stripTrailingZeros(), expectedBalance.stripTrailingZeros());
        validateResolvedRequest(requestId, HttpStatus.BAD_REQUEST);
    }

    /**
     * Test for Account not found.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_AccountNotFound() throws MoneyTransferException {
        var amount = BigDecimal.ONE;
        var nonExistingAccountId = UUID.randomUUID();
        var expectedBalance = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> moneyTransferService.transfer(new NewTransferDto(requestId, sourceAccount.getAccountId(), nonExistingAccountId, amount), ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        var actualBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualBalance.stripTrailingZeros(), expectedBalance.stripTrailingZeros());
        validateResolvedRequest(requestId, HttpStatus.NOT_FOUND);
    }

    /**
     * Test for the idempotent behavior of a successful transfer request.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testIdempotency_SuccessfulRequest() throws MoneyTransferException {
        var requestId = UUID.randomUUID();
        var amount = sourceAccount.getBalance();
        var newTransferDto = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        var transaction1 = moneyTransferService.transfer(newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        var transaction2 = moneyTransferService.transfer(newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    /**
     * Test for the idempotent behavior of a failed transfer request.
     */
    @Test
    public void testIdempotency_FailedRequest() {
        var requestId = UUID.randomUUID();
        var amount = sourceAccount.getBalance().multiply(BigDecimal.TEN);
        var newTransferDto = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        var exception1 = assertThrows(MoneyTransferException.class, () -> moneyTransferService.transfer(newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        var exception2 = assertThrows(MoneyTransferException.class, () -> moneyTransferService.transfer(newTransferDto, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        assertEquals(exception2.getMessage(), exception1.getMessage());
        assertEquals(exception2.getHttpStatus(), exception1.getHttpStatus());

    }

    /**
     * Test for payload idempotency
     *
     * @throws MoneyTransferException
     */
    @Test
    public void testIdempotency_WrongPayload() throws MoneyTransferException {
        var initialBalance = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        var newTransferDto1 = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), initialBalance);
        var newTransferDto2 = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), BigDecimal.ZERO);
        moneyTransferService.transfer(newTransferDto1, ConcurrencyControlMode.SERIALIZABLE_ISOLATION);
        var exception = assertThrows(RequestConflictException.class, () -> moneyTransferService.transfer(newTransferDto2, ConcurrencyControlMode.SERIALIZABLE_ISOLATION));
        assertTrue(exception.getMessage().contains("The JSON body does not match"));
        assertEquals(exception.getHttpStatus(), HttpStatus.CONFLICT);
    }

    /**
     * Validates the resolved request fields.
     *
     * @param id
     * @param httpStatus
     */
    private void validateResolvedRequest(UUID id, HttpStatus httpStatus) {
        Optional<TransactionRequest> retrievedRequest = transactionRequestRepository.findById(id);
        assertTrue(retrievedRequest.isPresent());
        assertEquals(retrievedRequest.get().getTransactionRequestStatus(), TransactionRequestStatus.COMPLETED);
        assertEquals(retrievedRequest.get().getHttpStatus(), httpStatus);
        if (!httpStatus.is2xxSuccessful())
            assertNull(retrievedRequest.get().getTransaction());
        else
            assertNotNull(retrievedRequest.get().getTransaction());
    }

    private BigDecimal retrievePersistedBalance(UUID accountId) throws ResourceNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, accountId))
                .getBalance();
    }
}

