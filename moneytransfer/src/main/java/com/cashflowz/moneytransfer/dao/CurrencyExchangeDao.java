package com.cashflowz.moneytransfer.dao;

import com.cashflowz.moneytransfer.dto.ExchangeRatesResponse;
import com.cashflowz.moneytransfer.enums.Currency;
import org.springframework.http.ResponseEntity;

/**
 * Represents a Data Access Object (DAO) for retrieving currency exchange rates.
 * This interface provides methods to interact with the currency exchange API.
 */
public interface CurrencyExchangeDao {
    /**
     * Retrieves the response from a third-party API for exchanging the source to the target {@link Currency}.
     *
     * @param sourceCurrency The source currency to be exchanged.
     * @param targetCurrency The target currency for the exchange.
     * @return A {@link ResponseEntity} containing an {@link ExchangeRatesResponse} object, which encapsulates the exchange rates data.
     */
    ResponseEntity<ExchangeRatesResponse> get(Currency sourceCurrency, Currency targetCurrency);
}
