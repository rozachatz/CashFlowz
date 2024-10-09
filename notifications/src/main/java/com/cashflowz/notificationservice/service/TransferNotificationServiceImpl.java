package com.cashflowz.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransferNotificationServiceImpl implements TransferNotificationService{
    @Override
    public void sendNotification(String message) {
        log.info(message);
    }
}
