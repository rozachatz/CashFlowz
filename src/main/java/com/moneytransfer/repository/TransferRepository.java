package com.moneytransfer.repository;

import com.moneytransfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Modifying
    @Query("UPDATE Transfer t SET t.transferStatus = com.moneytransfer.enums.TransferStatus.REFUNDED WHERE t.transferId =: transferId")
    void updateRefundedTransfer(UUID transferId);
}
