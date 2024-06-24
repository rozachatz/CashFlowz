/**
 * Test class for {@link com.moneytransfer.service.MoneyTransferServiceImpl}
 * This test uses embedded h2 db.
 */
package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
public class MoneyTransferServiceTest {
    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private MoneyTransferService moneyTransferService;

    @Autowired
    private AccountRepository accountRepository;

    private Account sourceAccount;

    private Account targetAccount;

    private Account targetAccount1;


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
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccount1.getBalance().add(amount);
        moneyTransferService.transferSerializable(new NewTransferDto(UUID.randomUUID(), sourceAccount.getAccountId(), targetAccount1.getAccountId(), amount));
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var actualTargetBalance = retrievePersistedBalance(targetAccount1.getAccountId());
        expectedTargetBalance = expectedTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        actualTargetBalance = actualTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        assertEquals(actualTargetBalance, expectedTargetBalance);
    }

    /**
     * Test for happy path with currency exchange.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_HappyPath_ExchangeCurrency() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccount.getBalance().add(currencyExchangeService.exchange(amount, sourceAccount.getCurrency(), targetAccount.getCurrency()));
        moneyTransferService.transferSerializable(new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount));
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var actualTargetBalance = retrievePersistedBalance(targetAccount.getAccountId());
        expectedTargetBalance = expectedTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        actualTargetBalance = actualTargetBalance.setScale(2, RoundingMode.HALF_EVEN);
        assertEquals(actualTargetBalance, expectedTargetBalance);
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
        assertThrows(InsufficientBalanceException.class, () -> moneyTransferService.transferSerializable(new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount)));
        var actualSourceBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        var expectedSourceBalance = sourceAccount.getBalance();
        assertEquals(actualSourceBalance.stripTrailingZeros(), expectedSourceBalance.stripTrailingZeros());
        var expectedTargetBalance = targetAccount.getBalance();
        var actualTargetBalance = retrievePersistedBalance(targetAccount.getAccountId()).stripTrailingZeros();
        assertEquals(actualTargetBalance.stripTrailingZeros(), expectedTargetBalance.stripTrailingZeros());
    }

    /**
     * Test for same account.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_transferSerializableSameAccount() throws MoneyTransferException {
        var amount = BigDecimal.ONE;
        var expectedBalance = sourceAccount.getBalance();
        var requestId = UUID.randomUUID();
        assertThrows(SameAccountException.class, () -> moneyTransferService.transferSerializable(new NewTransferDto(requestId, sourceAccount.getAccountId(), sourceAccount.getAccountId(), amount)));
        var actualBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualBalance.stripTrailingZeros(), expectedBalance.stripTrailingZeros());
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
        assertThrows(ResourceNotFoundException.class, () -> moneyTransferService.transferSerializable(new NewTransferDto(requestId, sourceAccount.getAccountId(), nonExistingAccountId, amount)));
        var actualBalance = retrievePersistedBalance(sourceAccount.getAccountId());
        assertEquals(actualBalance.stripTrailingZeros(), expectedBalance.stripTrailingZeros());
    }

    private BigDecimal retrievePersistedBalance(UUID accountId) throws ResourceNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, accountId))
                .getBalance();
    }
}

