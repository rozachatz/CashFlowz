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
    @KafkaListener(topics = "${spring.kafka.consumer.topic.notifications}",  groupId = "${spring.kafka.consumer.group-id.notifications}")
    public void sendNotification(TransferCompletedEvent event) {
        transferNotificationService.sendNotification(event.message());
    }
}
