package com.moneytransfer.entity;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.enums.TransactionRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an idempotent transfer request.
 * This entity is used to store information about transfer requests.
 */
@Entity
@Table(name = "transaction_requests")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransactionRequest
        implements Serializable {
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

    public NewTransferDto toNewTransferDto() {
        return new NewTransferDto(requestId, sourceAccountId, targetAccountId, amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, sourceAccountId, targetAccountId, amount.stripTrailingZeros(), httpStatus,
                infoMessage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        TransactionRequest that = (TransactionRequest) obj;
        return Objects.equals(requestId, that.requestId) && Objects.equals(sourceAccountId, that.sourceAccountId)
                && Objects.equals(targetAccountId, that.targetAccountId) && Objects.equals(amount.stripTrailingZeros(),
                that.amount.stripTrailingZeros()) && Objects.equals(httpStatus, that.httpStatus) && Objects.equals(
                infoMessage, that.infoMessage);
    }
}
