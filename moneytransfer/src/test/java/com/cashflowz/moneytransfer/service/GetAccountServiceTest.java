package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.dto.GetAccountsForNewTransferDto;
import com.cashflowz.moneytransfer.dto.PageResponseDto;
import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.repository.AccountRepository;
import com.cashflowz.moneytransfer.service.GetAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetAccountServiceTest {
    @InjectMocks
    GetAccountServiceImpl getAccountService;

    @Mock
    AccountRepository accountRepository;

    List<Account> accounts;

    @BeforeEach
    public void before() {
        Account sourceAccount = new Account(0, UUID.randomUUID(), "test", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        Account targetAccount = new Account(0, UUID.randomUUID(), "test", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        accounts = List.of(sourceAccount, targetAccount);
    }

    @Test
    void testGetAccounts() {
        int maxRecords = 5;
        doReturn(new PageImpl<>(accounts)).when(accountRepository).findAll(Pageable.ofSize(maxRecords));
        PageResponseDto<Account> result = getAccountService.getAccounts(maxRecords);
        assertNotNull(result);
        assertEquals(accounts, result.content());
    }

    @Test
    void testGetAccountPairByIds() throws ResourceNotFoundException {
        Account sourceAccount = accounts.get(0);
        Account targetAccount = accounts.get(1);
        doReturn(Optional.of(getAccountsDtoObj())).when(accountRepository).findByIds(sourceAccount.getAccountId(), targetAccount.getAccountId());
        GetAccountsForNewTransferDto result = getAccountService.getAccountPairByIds(sourceAccount.getAccountId(), targetAccount.getAccountId());
        assertEquals(accounts, List.of(result.getSourceAccount(), result.getTargetAccount()));
    }


    @Test
    void testGetAccountById_Success() throws ResourceNotFoundException {
        Account account = accounts.get(0);
        when(accountRepository.findById(account.getAccountId())).thenReturn(Optional.of(account));
        Account result = getAccountService.getAccountById(account.getAccountId());
        assertEquals(account, result);
    }

    @Test
    void testGetAccountById_NotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> getAccountService.getAccountById(accountId));
    }

    private GetAccountsForNewTransferDto getAccountsDtoObj() {
        return new GetAccountsForNewTransferDto() {
            @Override
            public Account getSourceAccount() {
                return accounts.get(0);
            }

            @Override
            public Account getTargetAccount() {
                return accounts.get(1);
            }
        };
    }
}
