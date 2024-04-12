package com.moneytransfer.idempotent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is an idempotent transfer request.
 * Idempotent requests are those that can be safely retried without leading to duplicate actions or unintended side effects.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IdempotentTransferRequest {
}
