package com.moneytransfer.dto;

import com.moneytransfer.entity.TransferRequest;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Dto for a new {@link TransferRequest}.
 *
 * @param transferRequestId
 * @param sourceAccountId
 * @param targetAccountId
 * @param amount
 */
public record NewTransferDto(UUID transferRequestId, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
    @Override
    public int hashCode() {
        return Objects.hash(transferRequestId, sourceAccountId, targetAccountId, amount.stripTrailingZeros());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        NewTransferDto that = (NewTransferDto) obj;
        return Objects.equals(transferRequestId, that.transferRequestId) && Objects.equals(sourceAccountId, that.sourceAccountId)
                && Objects.equals(targetAccountId, that.targetAccountId) && Objects.equals(amount.stripTrailingZeros(), that.amount.stripTrailingZeros());
    }
}
