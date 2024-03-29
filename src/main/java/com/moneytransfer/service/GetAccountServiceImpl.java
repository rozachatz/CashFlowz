package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link GetAccountService}
 */
@Service
@RequiredArgsConstructor
class GetAccountServiceImpl implements GetAccountService {
    /**
     * The Account repository.
     */
    private final AccountRepository accountRepository;

    /**
     * Get Account by accountId.
     *
     * @param accountId
     * @return Transaction
     * @throws ResourceNotFoundException
     */
    public Account getAccountById(final UUID accountId) throws ResourceNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(accountId));
    }

    /**
     * Gets the source/target account pair by their ids.
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIds(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(List.of(sourceAccountId,targetAccountId)));
    }

    /**
     * Gets account records.
     *
     * @param maxRecords
     * @return Accounts
     */
    public Page<Account> getAccounts(final int maxRecords) {
        var pageRequest = PageRequest.of(0, maxRecords);
        return accountRepository.findAll(pageRequest);
    }

    /**
     * Gets the {@link TransferAccountsDto} with optimistic locking
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */

    public TransferAccountsDto getAccountsByIdsOptimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(List.of(sourceAccountId,targetAccountId)));
    }

    /**
     * Gets the {@link TransferAccountsDto} with pessimistic locking
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */
    public TransferAccountsDto getAccountsByIdsPessimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(List.of(sourceAccountId, targetAccountId)));
    }
}
