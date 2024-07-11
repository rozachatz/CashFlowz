package com.moneytransfer.service;

import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.exceptions.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service that gets {@link Transfer} entities.
 */
public interface GetTransferService {
    /**
     * Gets a transfer by id.
     *
     * @param transferId The id of the Transfer.
     * @return {@link Transfer}
     * @throws ResourceNotFoundException if no {@link Transfer} was found for this id.
     */
    Transfer getTransferById(UUID transferId) throws ResourceNotFoundException;

    /**
     * Gets {@link Transfer} records.
     *
     * @param maxRecords The maximum number of records that will be returned.
     * @return PageResponseDto for the transactions.
     */
    PageResponseDto<Transfer> getTransfers(int maxRecords);

}
