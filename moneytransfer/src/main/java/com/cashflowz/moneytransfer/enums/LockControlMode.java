package com.cashflowz.moneytransfer.enums;

import com.cashflowz.moneytransfer.service.MoneyTransferService;
import com.cashflowz.moneytransfer.dto.NewTransferDto;

/**
 * Concurrency control for the money transfer operation.
 *
 * @see MoneyTransferService#transferWithLocking(NewTransferDto, LockControlMode)
 */
public enum LockControlMode {
    OPTIMISTIC_LOCKING,
    PESSIMISTIC_LOCKING
}
