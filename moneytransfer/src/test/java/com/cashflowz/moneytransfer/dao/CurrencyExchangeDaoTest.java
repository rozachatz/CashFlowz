package com.cashflowz.moneytransfer.dao;


import com.cashflowz.moneytransfer.dao.CurrencyExchangeDaoImpl;
import com.cashflowz.moneytransfer.enums.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CurrencyExchangeDaoImpl.class})
public class CurrencyExchangeDaoTest {
    @Autowired
    CurrencyExchangeDaoImpl currencyExchangeClient;

    @Test
    public void testExecute() {
        assertTrue(currencyExchangeClient.get(Currency.EUR, Currency.CAD).getStatusCode().is2xxSuccessful());
    }
}
