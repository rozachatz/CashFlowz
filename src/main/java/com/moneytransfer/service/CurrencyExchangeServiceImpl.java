package com.moneytransfer.service;

import com.moneytransfer.dto.ExchangeRatesResponse;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Implementation of {@link CurrencyExchangeService}
 */
@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    @Value("${application.freecurrencyapi.url}")
    private String url;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Performs the currency exchange.
     *
     * @param amount
     * @param sourceCurrency
     * @param targetCurrency
     * @return
     * @throws MoneyTransferException
     */
    public BigDecimal exchangeCurrency(BigDecimal amount, final Currency sourceCurrency, final Currency targetCurrency) throws MoneyTransferException {
        var endpoint = String.format("%1$s&currencies=%2$s&base_currency=%3$s", url, targetCurrency.name(), sourceCurrency.name());
        ResponseEntity<ExchangeRatesResponse> responseEntity = restTemplate.getForEntity(endpoint, ExchangeRatesResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            ExchangeRatesResponse response = responseEntity.getBody();
            if (response != null && response.data() != null) {
                return amount.multiply(BigDecimal.valueOf(response.data().get(targetCurrency.name())));
            }
        }
        throw new MoneyTransferException("Error occurred while exchanging currency!");
    }

}
