package com.moneytransfer.repository;

import com.moneytransfer.dto.CompletedRequestDto;
import com.moneytransfer.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, UUID> {
    /**
     * Completes a {@link TransactionRequest} by updating its status to {@link com.moneytransfer.enums.TransactionRequestStatus#COMPLETED},
     * and setting its transaction, httpStatus, and  infoMessage fields with the provided data from the {@link CompletedRequestDto}.
     *
     * @param dto the DTO containing the data to complete the request.
     */
    @Modifying
    @Query("UPDATE TransactionRequest r SET r.transactionRequestStatus = com.moneytransfer.enums.TransactionRequestStatus.COMPLETED, r.transaction = :#{#dto.transaction}, r.httpStatus = :#{#dto.httpStatus},  r.infoMessage = :#{#dto.infoMessage} WHERE r.requestId = :#{#dto.requestId}")
    void completeRequest(@Param("dto") CompletedRequestDto dto);
}
