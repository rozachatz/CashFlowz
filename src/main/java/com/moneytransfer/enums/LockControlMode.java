package com.moneytransfer.enums;

import com.moneytransfer.dto.NewTransferDto;

/**
 * Concurrency control for the money transfer operation.
 *
 * @see com.moneytransfer.service.MoneyTransferService#transferWithLocking(NewTransferDto, LockControlMode)
 */
public enum LockControlMode {
    OPTIMISTIC_LOCKING,
    PESSIMISTIC_LOCKING
}
