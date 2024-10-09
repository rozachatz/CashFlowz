package com.cashflowz.moneytransfer.entity;

import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.enums.TransferRequestStatus;
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
@Table(name = "transfer_request")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransferRequest implements Serializable {
    @Id
    private UUID transferRequestId;
    private BigDecimal amount;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private TransferRequestStatus transferRequestStatus;
    private HttpStatus httpStatus;
    private String infoMessage;
    @OneToOne
    @JoinColumn(name = "transfer_id", referencedColumnName = "transferId")
    private Transfer transfer;


    public NewTransferDto toNewTransferDto() {
        return new NewTransferDto(transferRequestId, sourceAccountId, targetAccountId, amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferRequestId, sourceAccountId, targetAccountId, amount.stripTrailingZeros(), httpStatus,
                infoMessage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        TransferRequest that = (TransferRequest) obj;
        return Objects.equals(transferRequestId, that.transferRequestId) && Objects.equals(sourceAccountId, that.sourceAccountId)
                && Objects.equals(targetAccountId, that.targetAccountId) && Objects.equals(amount.stripTrailingZeros(),
                that.amount.stripTrailingZeros()) && Objects.equals(httpStatus, that.httpStatus) && Objects.equals(
                infoMessage, that.infoMessage);
    }
}
