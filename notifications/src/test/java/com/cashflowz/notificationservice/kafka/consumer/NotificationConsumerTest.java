package com.cashflowz.notificationservice.kafka.consumer;

import com.cashflowz.common.events.TransferCompletedEvent;
import com.cashflowz.notificationservice.service.TransferNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class NotificationConsumerTest {

    @Mock
    TransferNotificationService notificationService;
    @InjectMocks
    NotificationConsumer consumer;

    @Test
    public void testHandleMessage() {
        String message = "Test notification message";
        consumer.sendNotification(new TransferCompletedEvent(message));
        verify(notificationService).sendNotification(message);
    }
}
