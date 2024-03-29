package com.moneytransfer.service;

import com.moneytransfer.dto.TransferAccountsDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service that gets {@link Account} entities.
 */
public interface GetAccountService {
    Account getAccountById(final UUID accountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    Page<Account> getAccounts(int maxRecords);

    TransferAccountsDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    TransferAccountsDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;
}
