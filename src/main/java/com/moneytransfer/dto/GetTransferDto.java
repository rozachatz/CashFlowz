package com.moneytransfer.dto;

import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dto for a {@link Transaction} entity.
 *
 * @param transactionId
 * @param sourceAccountId
 * @param targetAccountId
 * @param amount
 * @param currency
 */
public record GetTransferDto(UUID transactionId, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                             Currency currency) {
}


