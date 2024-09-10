package com.moneytransfer.service;

import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;

import java.math.BigDecimal;

/**
 * Service for exchanging source currency in a transfer operation.
 */
public interface CurrencyExchangeService {
    /**
     * Performs the currency exchange operation from source to target {@link Currency}.
     *
     * @param amount         The amount to be exchanged.
     * @param sourceCurrency The source {@link Currency}.
     * @param targetCurrency The target {@link Currency}.
     * @return The amount exchanged in the target currency.
     * @throws MoneyTransferException If the exchange rate cannot be fetched.
     */
    BigDecimal exchange(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency) throws MoneyTransferException;
}
