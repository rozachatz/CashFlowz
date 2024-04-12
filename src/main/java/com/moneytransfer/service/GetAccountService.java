package com.moneytransfer.service;

import com.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.exceptions.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service that retrieves {@link Account} resources.
 */
public interface GetAccountService {
    /**
     * Gets an account by id.
     *
     * @param accountId
     * @return {@link Account}
     * @throws ResourceNotFoundException if no {@link Account} was found for this id.
     */
    Account getAccountById(UUID accountId) throws ResourceNotFoundException;

    /**
     * Gets the source and target {@link Account} entities by their ids.
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return GetAccountsForNewTransferDto containing the accounts.
     * @throws ResourceNotFoundException if an {@link Account} was not found for this id.
     */
    GetAccountsForNewTransferDto getAccountsByIds(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    /**
     * Gets {@link Account} records.
     *
     * @param maxRecords The maximum number of records that will be returned.
     * @return PageResponseDto for the accounts.
     */
    PageResponseDto<Account> getAccounts(int maxRecords);

    /**
     * Retrieves the {@link GetAccountsForNewTransferDto} containing source and target accounts
     * and ensures that resources will be locked using optimistic locking.
     * For more information see <a href="https://www.baeldung.com/jpa-optimistic-locking">...</a>.
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */
    GetAccountsForNewTransferDto getAccountsByIdsOptimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;

    /**
     * Retrieves the {@link GetAccountsForNewTransferDto} containing source and target accounts
     * and ensures that resources will be locked using pessimistic locking.
     * For more information see <a href="https://www.baeldung.com/jpa-pessimistic-locking">...</a>.
     *
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     * @throws ResourceNotFoundException
     */
    GetAccountsForNewTransferDto getAccountsByIdsPessimistic(UUID sourceAccountId, UUID targetAccountId) throws ResourceNotFoundException;
}
