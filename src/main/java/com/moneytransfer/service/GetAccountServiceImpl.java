package com.moneytransfer.service;

import com.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link GetAccountService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
class GetAccountServiceImpl implements GetAccountService {
    private final AccountRepository accountRepository;

    public Account getAccountById(final UUID accountId) throws ResourceNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, accountId));
    }


    public GetAccountsForNewTransferDto getAccountsByIds(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId).orElseThrow(() -> new ResourceNotFoundException(Account.class, sourceAccountId, targetAccountId));
    }


    public PageResponseDto<Account> getAccounts(final int maxRecords) {
        var pageRequest = PageRequest.of(0, maxRecords);
        List<Account> accounts = accountRepository.findAll(pageRequest).toList();
        return new PageResponseDto<>(accounts);
    }


    public GetAccountsForNewTransferDto getAccountsByIdsOptimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, sourceAccountId, targetAccountId));
    }


    public GetAccountsForNewTransferDto getAccountsByIdsPessimistic(final UUID sourceAccountId,
                                                                    final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, sourceAccountId, targetAccountId));
    }
}
