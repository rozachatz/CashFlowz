package com.cashflowz.moneytransfer.dto;

import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto that retains information for an {@link Account} entity.
 *
 * @param accountId
 * @param balance
 * @param currency
 */
public record GetAccountDto(UUID accountId, BigDecimal balance, Currency currency) {
}


