package com.moneytransfer.idempotent.event;

import org.springframework.context.ApplicationEvent;

public abstract class IdempotentTransferRequestEvent extends ApplicationEvent {
    public IdempotentTransferRequestEvent(Object source) {
        super(source);
    }
}
