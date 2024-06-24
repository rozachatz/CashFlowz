/**
 * Test class for {@link com.moneytransfer.service.CurrencyExchangeServiceImpl}
 */
package com.moneytransfer.service;

import com.moneytransfer.dao.CurrencyExchangeDao;
import com.moneytransfer.dao.CurrencyExchangeDaoImpl;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;
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
