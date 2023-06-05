package com.moneytransactions.moneytransfer.dto;

import java.math.BigDecimal;

public record TransferRequestDTO(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
}
