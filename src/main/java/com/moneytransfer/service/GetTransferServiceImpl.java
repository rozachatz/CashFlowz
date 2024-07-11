package com.moneytransfer.service;

import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation for {@link GetTransferService}.
 */
@Service
@RequiredArgsConstructor
class GetTransferServiceImpl implements GetTransferService {
    private final TransferRepository transferRepository;

    public PageResponseDto<Transfer> getTransfers(final int maxRecords) {
        List<Transfer> transfers = transferRepository.findAll(Pageable.ofSize(maxRecords)).toList();
        return new PageResponseDto<>(transfers);
    }

    public Transfer getTransferById(final UUID transferId) throws ResourceNotFoundException {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException(Transfer.class, transferId));
    }

}
