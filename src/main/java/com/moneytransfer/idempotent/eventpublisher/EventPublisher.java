package com.moneytransfer.idempotent.eventpublisher;

import jakarta.validation.constraints.NotNull;

public interface EventPublisher <T> {
        void publishEvent(@NotNull T applicationEvent);
}

