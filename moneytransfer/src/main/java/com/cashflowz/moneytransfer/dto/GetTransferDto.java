package com.cashflowz.moneytransfer.dto;

import com.cashflowz.moneytransfer.entity.Transfer;
import com.cashflowz.moneytransfer.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto for a {@link Transfer} entity.
 *
 * @param transferId
 * @param sourceAccountId
 * @param targetAccountId
 * @param amount
 * @param currency
 */
public record GetTransferDto(UUID transferId, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                             Currency currency) {
}


