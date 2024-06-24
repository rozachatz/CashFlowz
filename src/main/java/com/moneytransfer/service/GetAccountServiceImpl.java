package com.moneytransfer.service;

import com.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    public PageResponseDto<Account> getAccounts(int maxRecords) {
        List<Account> accounts = accountRepository.findAll(Pageable.ofSize(maxRecords)).toList();
        return new PageResponseDto<>(accounts);
    }

    public GetAccountsForNewTransferDto getAccountPairByIds(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIds(sourceAccountId, targetAccountId).orElseThrow(() -> new ResourceNotFoundException(Account.class, sourceAccountId, targetAccountId));
    }


    public GetAccountsForNewTransferDto getAccountPairByIdsOptimistic(final UUID sourceAccountId, final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockOptimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, sourceAccountId, targetAccountId));
    }


    public GetAccountsForNewTransferDto getAccountPairByIdsPessimistic(final UUID sourceAccountId,
                                                                       final UUID targetAccountId) throws ResourceNotFoundException {
        return accountRepository.findByIdAndLockPessimistic(sourceAccountId, targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(Account.class, sourceAccountId, targetAccountId));
    }
}
