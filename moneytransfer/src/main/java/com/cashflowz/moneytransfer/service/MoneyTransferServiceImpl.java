package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.exceptions.InsufficientBalanceException;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.exceptions.SameAccountException;
import com.cashflowz.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.cashflowz.moneytransfer.repository.TransferRepository;
import com.fasterxml.uuid.Generators;
import com.cashflowz.moneytransfer.dao.CurrencyExchangeDao;
import com.cashflowz.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.entity.Transfer;
import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.enums.LockControlMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Implementation for {@link MoneyTransferService}.
 */
@Service
@RequiredArgsConstructor
class MoneyTransferServiceImpl implements MoneyTransferService {
    private final CurrencyExchangeService currencyExchangeService;
    private final GetAccountService getAccountService;
    private final TransferRepository transferRepository;

    /**
     * Transfer with optimistic or pessimistic locking.
     *
     * @param newTransferDto  The dto representing the transfer request.
     * @param lockControlMode The lock control mode to use for the transfer operation.
     * @return a new {@link Transfer}
     * @throws MoneyTransferException for a business error.
     */
    @Transactional(propagation = REQUIRES_NEW)
    public Transfer transferWithLocking(final NewTransferDto newTransferDto, final LockControlMode lockControlMode) throws MoneyTransferException {
        return switch (lockControlMode) {
            case OPTIMISTIC_LOCKING -> transferOptimistic(newTransferDto);
            case PESSIMISTIC_LOCKING -> transferPessimistic(newTransferDto);
        };
    }


    /**
     * Transfer money with no serializable isolation.
     * For more information see <a href="https://www.baeldung.com/spring-transactional-propagation-isolation#5-serializable-isolation">...</a>.
     *
     * @param newTransferDto The dto representing the new money transfer.
     * @return a new {@link Transfer}
     * @throws MoneyTransferException for a business error.
     */
    @IdempotentTransferRequest
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = REQUIRES_NEW)
    public Transfer transferSerializable(final NewTransferDto newTransferDto) throws MoneyTransferException {
        return transfer(newTransferDto);
    }


    /**
     * Transfer money with no resource locking.
     *
     * @param newTransferDto The dto representing the new money transfer
     * @return a new {@link Transfer}
     * @throws MoneyTransferException for a business error.
     */
    private Transfer transfer(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountPairByIds(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with pessimistic locking guarantees that resources will be locked with pessimistic locking.
     * For more information see <a href="https://www.baeldung.com/jpa-optimistic-locking">...</a>.
     *
     * @param newTransferDto The dto representing the new money transfer
     * @return a new {@link Transfer}
     * @throws MoneyTransferException for a business error
     */
    private Transfer transferOptimistic(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountPairByIdsOptimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }

    /**
     * Transfer money with pessimistic locking.
     * For more information see <a href="https://www.baeldung.com/jpa-pessimistic-locking">...</a>.
     *
     * @param newTransferDto The dto representing the new money transfer.
     * @return a new {@link Transfer}
     * @throws MoneyTransferException for a business error
     */
    private Transfer transferPessimistic(final NewTransferDto newTransferDto) throws MoneyTransferException {
        var transferAccountsDto = getAccountService.getAccountPairByIdsPessimistic(newTransferDto.sourceAccountId(), newTransferDto.targetAccountId());
        return performTransfer(transferAccountsDto, newTransferDto);
    }

    /**
     * Validate and persist the resulting Transfer.
     *
     * @param getAccountsForNewTransferDto The projection interface containing the source and target {@link Account} entities.
     * @param newTransferDto               The dto representing the new money transfer.
     * @return a new {@link Transfer}
     * @throws MoneyTransferException for a business error
     */
    private Transfer performTransfer(final GetAccountsForNewTransferDto getAccountsForNewTransferDto, final NewTransferDto newTransferDto) throws MoneyTransferException {
        validateTransfer(getAccountsForNewTransferDto, newTransferDto.amount());
        return persistSuccessfulTransfer(getAccountsForNewTransferDto, newTransferDto.amount());
    }

    /**
     * Validates a money transfer operation according to acceptance criteria.
     *
     * <p>
     * This method checks whether the money transfer meets specific criteria before proceeding with the transfer.
     * If the transfer does not meet the criteria, appropriate exceptions are thrown to indicate the failure.
     * </p>
     * <p>
     * Acceptance Criteria:
     * <ul>
     *   <li>Happy Path.</li>
     *   <li>The source and target accounts must be different.</li>
     *   <li>The source account must have sufficient balance to cover the transfer amount.</li>
     *   <li>Source/Target account not found.</li>
     *   <li>Currency exchange operation failure.</li>
     * </ul>
     * </p>
     *
     * @param accounts The source and target {@link Account} content.
     * @param amount   The amount to be transferred.
     * @throws MoneyTransferException for a business error
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


    private Transfer persistSuccessfulTransfer(final GetAccountsForNewTransferDto getAccountsForNewTransferDto, final BigDecimal amount) throws MoneyTransferException {
        var sourceAccount = getAccountsForNewTransferDto.getSourceAccount();
        var targetAccount = getAccountsForNewTransferDto.getTargetAccount();
        transferAndExchange(sourceAccount, targetAccount, amount);
        var currency = sourceAccount.getCurrency();
        return transferRepository.save(new Transfer(Generators.timeBasedEpochGenerator().generate(), sourceAccount, targetAccount, amount, currency));
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
            return currencyExchangeService.exchange(amount, sourceAccount.getCurrency(), targetAccount.getCurrency());
        }
        return amount;
    }

}
