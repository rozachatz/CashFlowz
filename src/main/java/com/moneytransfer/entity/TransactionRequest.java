package com.moneytransfer.entity;

import com.moneytransfer.enums.TransactionRequestStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;

/**
 *  Entity for creating an idempotent transfer request.
 */
@Entity
@Table(name = "transaction_requests")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransactionRequest implements Serializable {
    @Id
    private UUID requestId;
    private BigDecimal amount;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private TransactionRequestStatus transactionRequestStatus;
    private HttpStatus httpStatus;
    private String infoMessage;
    @OneToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "transactionId")
    private Transaction transaction;

    @Override
    public int hashCode() {
        return Objects.hash(requestId, sourceAccountId, targetAccountId, amount.stripTrailingZeros());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionRequest that = (TransactionRequest) obj;
        return  Objects.equals(requestId, that.requestId) &&
                Objects.equals(sourceAccountId, that.sourceAccountId) &&
                Objects.equals(targetAccountId, that.targetAccountId) &&
                Objects.equals(amount, that.amount);
    }
}
