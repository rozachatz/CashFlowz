package com.cashflowz.moneytransfer.repository;

import com.cashflowz.moneytransfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
}
