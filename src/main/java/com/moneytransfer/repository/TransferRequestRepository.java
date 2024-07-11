package com.moneytransfer.repository;

import com.moneytransfer.dto.CompletedTransferRequestDto;
import com.moneytransfer.entity.TransferRequest;
import com.moneytransfer.enums.TransferRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, UUID> {
    /**
     * Completes a {@link TransferRequest} by updating its status to {@link TransferRequestStatus#COMPLETED},
     * and setting its transfer, httpStatus, and  infoMessage fields with the provided data from the {@link CompletedTransferRequestDto}.
     *
     * @param dto the DTO containing the data to complete the request.
     */
    @Modifying
    @Query("UPDATE TransferRequest r SET r.transferRequestStatus = com.moneytransfer.enums.TransferRequestStatus.COMPLETED, " +
            "r.transfer = :#{#dto.transfer}, r.httpStatus = :#{#dto.httpStatus},  r.infoMessage = :#{#dto.infoMessage} WHERE r.transferRequestId = :#{#dto.transferRequestId}")
    void completeRequest(@Param("dto") CompletedTransferRequestDto dto);
}
