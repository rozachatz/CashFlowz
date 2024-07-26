package com.moneytransfer.idempotent.eventpublisher;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SpringEventPublisher implements EventPublisher<ApplicationEvent> {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishEvent(@NotNull final ApplicationEvent applicationEvent) {
        applicationEventPublisher.publishEvent(applicationEvent);
    }
}
