package com.cashflowz.common.events;

import java.io.Serializable;

public record TransferCompletedEvent(String message) implements Event, Serializable  {
}
