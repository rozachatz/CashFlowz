package com.moneytransfer.service;

import com.moneytransfer.dao.CurrencyExchangeDao;
import com.moneytransfer.dto.ExchangeRatesResponse;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link CurrencyExchangeService}
 */
@RequiredArgsConstructor
@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    private final CurrencyExchangeDao currencyExchangeDao;

    @Cacheable(cacheNames = "exchangeRatesCache", key = "#sourceCurrency.name()+#targetCurrency.name()")
    public BigDecimal exchange(BigDecimal amount, final Currency sourceCurrency, final Currency targetCurrency) throws MoneyTransferException {
        var response = currencyExchangeDao.get(sourceCurrency, targetCurrency);
        var rates = validateResponseAndGetRates(response);
        var targetRate = rates.get(targetCurrency.name());
        return amount.multiply(BigDecimal.valueOf(targetRate));
    }

    private Map<String, Double> validateResponseAndGetRates(ResponseEntity<ExchangeRatesResponse> response) throws MoneyTransferException {
        ExchangeRatesResponse exchangeRatesResponse = Optional.ofNullable(response)
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new MoneyTransferException("Cannot fetch exchange currency data from third party API!"));

        return Optional.ofNullable(exchangeRatesResponse.data())
                .orElseThrow(() -> new MoneyTransferException("Exchange currency data is missing from the response from third party API!"));
    }
}
