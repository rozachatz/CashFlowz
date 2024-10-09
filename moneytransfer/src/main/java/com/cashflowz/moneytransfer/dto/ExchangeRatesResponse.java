package com.cashflowz.moneytransfer.dto;

import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.dao.CurrencyExchangeDao;

import java.io.Serializable;
import java.util.Map;

/**
 * Dto for response from the {@link CurrencyExchangeDao}.
 *
 * @param data The data returned by the DAO.
 * @see CurrencyExchangeDao#get(Currency, Currency)
 */
public record ExchangeRatesResponse(Map<String, Double> data) implements Serializable {
}
