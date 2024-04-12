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
     * @param amount
     * @param sourceCurrency
     * @param targetCurrency
     * @return BigDecimal The exchanged rate.
     * @throws MoneyTransferException if the exchange rate cannot be fetched.
     */
    BigDecimal exchange(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency) throws MoneyTransferException;
}
