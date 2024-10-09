package com.cashflowz.moneytransfer.dto;

import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.entity.Transfer;

/**
 * A Dto representing the {@link Account} entities participating in a {@link Transfer}.
 * This Dto is used for interface projection.
 * For more information, see <a href="https://www.baeldung.com/spring-data-jpa-projections#interface-based-projections</a>
 */
public interface GetAccountsForNewTransferDto {
    Account getSourceAccount();

    Account getTargetAccount();
}
