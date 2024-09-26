package com.cashflowz.notificationservice.kafka.consumer;

import com.cashflowz.common.events.TransferCompletedEvent;
import com.cashflowz.notificationservice.service.TransferNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {
    private final TransferNotificationService transferNotificationService;
    @KafkaListener(topics = "console-notification", groupId = "notifications-group")
    public void sendNotification(TransferCompletedEvent event) {
        transferNotificationService.sendNotification(event.message());
    }
}
