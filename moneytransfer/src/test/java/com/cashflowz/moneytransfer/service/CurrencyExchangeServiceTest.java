/**
 * Test class for {@link com.cashflowz.moneytransfer.service.CurrencyExchangeServiceImpl}
 */
package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.dao.CurrencyExchangeDao;
import com.cashflowz.moneytransfer.dao.CurrencyExchangeDaoImpl;
import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.service.CurrencyExchangeService;
import com.cashflowz.moneytransfer.service.CurrencyExchangeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CurrencyExchangeServiceImpl.class, CurrencyExchangeDaoImpl.class})
public class CurrencyExchangeServiceTest {
    @Autowired
    private CurrencyExchangeService currencyExchangeDao;

    @Autowired
    private CurrencyExchangeDao currencyExchangeClient;

    @Test
    public void testAPI_Exchange() throws MoneyTransferException {
        BigDecimal amount = currencyExchangeDao.exchange(BigDecimal.valueOf(10), Currency.EUR, Currency.CAD);
        assertTrue(amount.compareTo(BigDecimal.ZERO) > 0);
    }
}
