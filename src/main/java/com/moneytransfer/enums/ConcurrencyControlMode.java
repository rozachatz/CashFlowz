package com.moneytransfer.enums;

import com.moneytransfer.dto.NewTransferDto;

/**
 * Concurrency control for the money transfer operation.
 *
 * @see com.moneytransfer.service.MoneyTransferService#transfer(NewTransferDto, ConcurrencyControlMode)
 */
public enum ConcurrencyControlMode {
    OPTIMISTIC_LOCKING,
    PESSIMISTIC_LOCKING,
    SERIALIZABLE_ISOLATION
}
