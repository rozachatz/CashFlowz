package com.moneytransfer.service;

import com.fasterxml.uuid.Generators;
import com.moneytransfer.dao.CurrencyExchangeDao;
import com.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.LockControlMode;
import com.moneytransfer.exceptions.InsufficientBalanceException;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.SameAccountException;
import com.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.moneytransfer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(propagation = NESTED)
    @IdempotentTransferRequest
    public Transaction transferWithLocking(final NewTransferDto newTransferDto, final LockControlMode lockControlMode) throws MoneyTransferException {
        return switch (lockControlMode) {
            case OPTIMISTIC_LOCKING -> transferOptimistic(newTransferDto);
            case PESSIMISTIC_LOCKING -> transferPessimistic(newTransferDto);
        };
    }


    @IdempotentTransferRequest
    @Transactional(propagation = NESTED, isolation = Isolation.SERIALIZABLE)
    public Transaction transferSerializable(final NewTransferDto newTransferDto) throws MoneyTransferException {
        return transfer(newTransferDto);
    }

    /**
     * Transfer money with no resource locking.
     *
     * @param newTransferDto
     * @return a new Transaction
     * @throws MoneyTransferException
     */
    private Transaction transfer(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountPairByIds(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
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
        var transferAccountsDto = getAccountService.getAccountPairByIdsOptimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
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
        var transferAccountsDto = getAccountService.getAccountPairByIdsPessimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }


    private Transaction performTransfer(final GetAccountsForNewTransferDto getAccountsForNewTransferDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        validateTransfer(getAccountsForNewTransferDto, newTransferDto.amount());
        return persistTransfer(getAccountsForNewTransferDto, newTransferDto.amount());
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
     * @param accounts The source and target {@link Account} content.
     * @param amount   The amount to be transferred.
     * @throws MoneyTransferException If the transfer fails to meet the acceptance criteria.
     */
    private void validateTransfer(final GetAccountsForNewTransferDto accounts, final BigDecimal amount) throws MoneyTransferException {
        validateAccountsNotSame(accounts);
        validateSufficientBalance(accounts.getSourceAccount(), amount);
    }

    private void validateAccountsNotSame(GetAccountsForNewTransferDto accounts) throws SameAccountException {
        if (accounts.getSourceAccount().getAccountId().equals(accounts.getTargetAccount().getAccountId())) {
            throw new SameAccountException(accounts.getSourceAccount().getAccountId());
        }
    }

    private void validateSufficientBalance(Account sourceAccount, BigDecimal amount) throws InsufficientBalanceException {
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(sourceAccount, amount);
        }
    }


    private Transaction persistTransfer(final GetAccountsForNewTransferDto getAccountsForNewTransferDto, final BigDecimal amount) throws MoneyTransferException {
        var sourceAccount = getAccountsForNewTransferDto.getSourceAccount();
        var targetAccount = getAccountsForNewTransferDto.getTargetAccount();
        transferAndExchange(sourceAccount, targetAccount, amount);
        var currency = sourceAccount.getCurrency();
        var transaction = new Transaction(Generators.timeBasedEpochGenerator().generate(), sourceAccount, targetAccount, amount, currency);
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
        if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
            return currencyExchangeDao.exchange(amount, sourceAccount.getCurrency(), targetAccount.getCurrency());
        }
        return amount;
    }

}
