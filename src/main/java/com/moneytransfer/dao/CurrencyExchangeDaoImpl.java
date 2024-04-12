package com.moneytransfer.dao;

import com.moneytransfer.dto.ExchangeRatesResponse;
import com.moneytransfer.enums.Currency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CurrencyExchangeDaoImpl implements CurrencyExchangeDao {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${application.freecurrencyapi.url}")
    private String url;

    public ResponseEntity<ExchangeRatesResponse> get(Currency sourceCurrency, Currency targetCurrency) {
        var endpoint = String.format("%1$s&currencies=%2$s&base_currency=%3$s", url, targetCurrency.name(),
                sourceCurrency.name());
        return restTemplate.getForEntity(endpoint, ExchangeRatesResponse.class);
    }
}
