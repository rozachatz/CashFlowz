package com.moneytransfer.service;

import com.fasterxml.uuid.Generators;
import com.moneytransfer.dao.CurrencyExchangeDao;
import com.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.ConcurrencyControlMode;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;

import static org.springframework.transaction.annotation.Propagation.NESTED;

/**
 * Implementation for {@link MoneyTransferService}.
 */
@Service
@RequiredArgsConstructor
class MoneyTransferServiceImpl implements MoneyTransferService {
    private final CurrencyExchangeService currencyExchangeDao;
    private final GetAccountService getAccountService;
    private final TransactionRepository transactionRepository;

    @IdempotentTransferRequest
    @Transactional(propagation = NESTED)
    public Transaction transfer(final NewTransferDto newTransferDto,
                                final ConcurrencyControlMode concurrencyControlMode) throws MoneyTransferException {
        return switch (concurrencyControlMode) {
            case SERIALIZABLE_ISOLATION -> {
                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(Isolation.SERIALIZABLE.value());
                yield transferSerializable(newTransferDto);
            }
            case OPTIMISTIC_LOCKING -> transferOptimistic(newTransferDto);
            case PESSIMISTIC_LOCKING -> transferPessimistic(newTransferDto);
        };
    }

    /**
     * Transfer money with serializable isolation guarantees that resources will be locked during the scope of the transaction.
     * For more information, see <a href="https://www.postgresql.org/docs/current/transaction-iso.html#XACT-SERIALIZABLE">...</a>.
     *
     * @param newTransferDto
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction transferSerializable(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountsByIds(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with pessimistic locking guarantees that resources will be locked with pessimistic locking.
     * For more information see <a href="https://www.baeldung.com/jpa-optimistic-locking">...</a>.
     *
     * @param newTransferDto
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction transferOptimistic(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountsByIdsOptimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with pessimistic locking.
     * For more information see <a href="https://www.baeldung.com/jpa-pessimistic-locking">...</a>.
     *
     * @param newTransferDto
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction transferPessimistic(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountsByIdsPessimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }


    private Transaction performTransfer(final GetAccountsForNewTransferDto getAccountsForNewTransferDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        validateTransfer(getAccountsForNewTransferDto, newTransferDto.amount());
        return persistValidTransfer(getAccountsForNewTransferDto, newTransferDto);
    }

    /**
     * Validates a money transfer operation according to acceptance criteria.
     * <p>
     * This method checks whether the money transfer meets specific criteria before proceeding with the transfer.
     * If the transfer does not meet the criteria, appropriate exceptions are thrown to indicate the failure.
     * </p>
     * <p>
     * Acceptance Criteria:
     * <ul>
     *   <li>The source and target accounts must be different.</li>
     *   <li>The source account must have sufficient balance to cover the transfer amount.</li>
     * </ul>
     * </p>
     *
     * @param accounts The source and target {@link Account} entities.
     * @param amount   The amount to be transferred.
     * @throws MoneyTransferException If the transfer fails to meet the acceptance criteria.
     */
    private void validateTransfer(final GetAccountsForNewTransferDto accounts, final BigDecimal amount) throws MoneyTransferException {
        if (accounts.getSourceAccount().getAccountId() == accounts.getTargetAccount().getAccountId()) {
            throw new SameAccountException(accounts.getSourceAccount().getAccountId());
        }
        if (accounts.getSourceAccount().getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(accounts.getSourceAccount(), amount);
        }
    }

    private Transaction persistValidTransfer(final GetAccountsForNewTransferDto getAccountsForNewTransferDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        var sourceAccount = getAccountsForNewTransferDto.getSourceAccount();
        var targetAccount = getAccountsForNewTransferDto.getTargetAccount();
        transferAndExchange(sourceAccount, targetAccount, newTransferDto.amount());
        var currency = sourceAccount.getCurrency();
        var transaction = new Transaction(Generators.timeBasedEpochGenerator().generate(), sourceAccount, targetAccount, newTransferDto.amount(), currency);
        return transactionRepository.save(transaction);
    }

    private void transferAndExchange(Account sourceAccount, Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        sourceAccount.debit(amount);
        var exchangedAmount = exchangeSourceCurrency(sourceAccount, targetAccount, amount);
        targetAccount.credit(exchangedAmount);
    }

    /**
     * Exchanges the specified amount from the source to the target {@link Currency}, if different.
     * <p>
     * This method exchanges the specified amount from the source currency to the target currency {@link Account},
     * if they are different. If the currencies are the same, the original amount is returned unchanged.
     * </p>
     * <p>
     * Note: Currency exchange is performed using the {@link CurrencyExchangeDao} provided by the system.
     * </p>
     *
     * @param sourceAccount The source {@link Account} from which the amount is being transferred.
     * @param targetAccount The target {@link Account} to which the amount will be credited.
     * @param amount        The amount to be exchanged.
     * @return The exchanged amount, if currencies are different; otherwise, the original amount.
     * @throws MoneyTransferException If an error occurs during the currency exchange process.
     */
    private BigDecimal exchangeSourceCurrency(final Account sourceAccount, final Account targetAccount, final BigDecimal amount) throws MoneyTransferException {
        var sourceCurrency = sourceAccount.getCurrency();
        var targetCurrency = targetAccount.getCurrency();
        if (sourceCurrency != targetCurrency) {
            return currencyExchangeDao.exchange(amount, sourceCurrency, targetCurrency);
        }
        return amount;
    }

}
