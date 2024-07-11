package com.moneytransfer.dto;

import com.moneytransfer.entity.Transfer;
import com.moneytransfer.enums.Currency;

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


