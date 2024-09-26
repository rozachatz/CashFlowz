package com.cashflowz.moneytransfer.repository;

import com.cashflowz.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.cashflowz.moneytransfer.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    String GET_ACCOUNTS_QUERY = """
            SELECT a1 as sourceAccount, a2 as targetAccount
            FROM Account a1, Account a2
            WHERE a1.id = :sourceAccountId AND a2.id = :targetAccountId
            """;

    /**
     * @param sourceAccountId
     * @param targetAccountId
     * @return
     */
    @Query(value = GET_ACCOUNTS_QUERY)
    Optional<GetAccountsForNewTransferDto> findByIds(UUID sourceAccountId, UUID targetAccountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = GET_ACCOUNTS_QUERY)
    Optional<GetAccountsForNewTransferDto> findByIdAndLockPessimistic(UUID sourceAccountId, UUID targetAccountId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query(value = GET_ACCOUNTS_QUERY)
    Optional<GetAccountsForNewTransferDto> findByIdAndLockOptimistic(UUID sourceAccountId, UUID targetAccountId);

}
