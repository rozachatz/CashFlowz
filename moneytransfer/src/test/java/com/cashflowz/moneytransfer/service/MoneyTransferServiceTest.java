/**
 * Test class for {@link com.cashflowz.moneytransfer.moneytransfer.service.MoneyTransferServiceImpl}
 * This test uses embedded h2 db.
 */
package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.exceptions.InsufficientBalanceException;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.exceptions.SameAccountException;
import com.cashflowz.moneytransfer.repository.AccountRepository;
import com.cashflowz.moneytransfer.service.CurrencyExchangeService;
import com.cashflowz.moneytransfer.service.MoneyTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
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
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1, topics = {"console-notification"})
public class MoneyTransferServiceTest {
    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @Autowired
    private MoneyTransferService moneyTransferService;

    @Autowired
    private AccountRepository accountRepository;

    private final Account sourceAccount, targetAccountDiffCurrency, targetAccount;

    public MoneyTransferServiceTest() {
        BigDecimal balance = BigDecimal.valueOf(10);
        sourceAccount = new Account(0, UUID.randomUUID(), "Name1", balance, Currency.EUR, LocalDateTime.now());
        targetAccountDiffCurrency = new Account(0, UUID.randomUUID(), "Name2", balance, Currency.USD, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), "Name3", balance, Currency.EUR, LocalDateTime.now());
    }


    @BeforeEach
    public void setup() {
        accountRepository.saveAll(List.of(targetAccountDiffCurrency, targetAccount, sourceAccount));
    }

    /**
     * Test for happy path.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_HappyPath() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        moneyTransferService.transferSerializable(new NewTransferDto(UUID.randomUUID(), sourceAccount.getAccountId(), targetAccount.getAccountId(), amount));
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccount.getBalance().add(amount);
        assertExpectedBalance(sourceAccount, expectedSourceBalance);
        assertExpectedBalance(targetAccount, expectedTargetBalance);
    }

    /**
     * Test for happy path with currency exchange.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_HappyPath_ExchangeCurrency() throws MoneyTransferException {
        var amount = sourceAccount.getBalance();
        moneyTransferService.transferSerializable(new NewTransferDto(UUID.randomUUID(), sourceAccount.getAccountId(), targetAccountDiffCurrency.getAccountId(), amount));
        var expectedSourceBalance = sourceAccount.getBalance().subtract(amount);
        var expectedTargetBalance = targetAccountDiffCurrency.getBalance().add(currencyExchangeService.exchange(amount, sourceAccount.getCurrency(), targetAccountDiffCurrency.getCurrency()));
        assertExpectedBalance(sourceAccount, expectedSourceBalance);
        assertExpectedBalance(targetAccountDiffCurrency, expectedTargetBalance);
    }

    /**
     * Test for insufficient balance.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_InsufficientBalance() throws MoneyTransferException {
        var amount = sourceAccount.getBalance().multiply(BigDecimal.valueOf(10));
        assertThrows(InsufficientBalanceException.class, () -> moneyTransferService.transferSerializable(new NewTransferDto( UUID.randomUUID(), sourceAccount.getAccountId(), targetAccount.getAccountId(), amount)));
        var expectedSourceBalance = sourceAccount.getBalance();
        var expectedTargetBalance = targetAccount.getBalance();
        assertExpectedBalance(sourceAccount, expectedSourceBalance);
        assertExpectedBalance(targetAccount, expectedTargetBalance);

    }


    /**
     * Test for same account.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_transferSameAccount() throws MoneyTransferException {
        assertThrows(SameAccountException.class, () -> moneyTransferService.transferSerializable(new NewTransferDto(UUID.randomUUID(), sourceAccount.getAccountId(), sourceAccount.getAccountId(), BigDecimal.ONE)));
        var expectedAccountBalance = sourceAccount.getBalance();
        assertExpectedBalance(sourceAccount, expectedAccountBalance);
    }

    /**
     * Test for Account not found.
     *
     * @throws MoneyTransferException
     */
    @Test
    public void test_AccountNotFound() throws MoneyTransferException {
        var nonExistingAccountId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> moneyTransferService.transferSerializable(new NewTransferDto(UUID.randomUUID(), sourceAccount.getAccountId(), nonExistingAccountId, BigDecimal.ONE)));
        var expectedBalance = sourceAccount.getBalance();
        assertExpectedBalance(sourceAccount, expectedBalance);
    }

    private void assertExpectedBalance(Account account, BigDecimal expectedBalance) throws ResourceNotFoundException {
        var actualBalance = getBalanceById(account.getAccountId()).setScale(2, RoundingMode.HALF_EVEN);
        expectedBalance = expectedBalance.setScale(2, RoundingMode.HALF_EVEN);
        assertEquals(actualBalance, expectedBalance);
    }

    private BigDecimal getBalanceById(UUID accountId) throws ResourceNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, accountId))
                .getBalance();
    }
}

