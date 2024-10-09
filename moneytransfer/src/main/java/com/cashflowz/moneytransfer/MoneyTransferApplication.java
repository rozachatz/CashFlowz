package com.cashflowz.moneytransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@EnableRetry
public class MoneyTransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyTransferApplication.class, args);

    }

}
