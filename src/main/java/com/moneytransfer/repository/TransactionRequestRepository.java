package com.moneytransfer.repository;

import com.moneytransfer.dto.ResolvedRequestDto;
import com.moneytransfer.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, UUID> {
    @Modifying
    @Query("UPDATE TransactionRequest r SET r.transactionRequestStatus = com.moneytransfer.enums.TransactionRequestStatus.COMPLETED, r.transaction = :#{#dto.transaction}, r.httpStatus = :#{#dto.httpStatus},  r.infoMessage = :#{#dto.infoMessage} WHERE r.requestId = :#{#dto.requestId}")
    void resolveTransactionRequest(@Param("dto") ResolvedRequestDto dto);

}
