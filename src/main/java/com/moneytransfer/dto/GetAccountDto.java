package com.moneytransfer.dto;

import com.moneytransfer.entity.Account;
import com.moneytransfer.enums.Currency;

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


