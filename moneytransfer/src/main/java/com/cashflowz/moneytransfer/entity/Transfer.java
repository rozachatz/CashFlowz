package com.cashflowz.moneytransfer.entity;

import com.cashflowz.moneytransfer.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity that represents a financial transfer between two {@link Account} entities.
 */
@Entity
@Table(name = "transfer")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Transfer implements Serializable {
    @Id
    private UUID transferId;

    @ManyToOne
    @JoinColumn(name = "source_account_id", referencedColumnName = "accountId")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "target_account_id", referencedColumnName = "accountId")
    private Account targetAccount;

    private BigDecimal amount;

    private Currency currency;


    @Override
    public int hashCode() {
        return Objects.hash(transferId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transfer other = (Transfer) obj;
        return Objects.equals(transferId, other.transferId);
    }
}
