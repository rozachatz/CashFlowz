package com.moneytransfer.dto;

import com.moneytransfer.enums.Currency;

import java.io.Serializable;
import java.util.Map;

/**
 * Dto for response from the {@link com.moneytransfer.dao.CurrencyExchangeDao}.
 *
 * @param data The data returned by the DAO.
 * @see com.moneytransfer.dao.CurrencyExchangeDao#get(Currency, Currency)
 */
public record ExchangeRatesResponse(Map<String, Double> data) implements Serializable {
}
