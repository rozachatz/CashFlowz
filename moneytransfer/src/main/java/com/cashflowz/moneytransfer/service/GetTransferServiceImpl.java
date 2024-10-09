package com.cashflowz.moneytransfer.service;

import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.repository.TransferRepository;
import com.cashflowz.moneytransfer.dto.PageResponseDto;
import com.cashflowz.moneytransfer.entity.Transfer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation for {@link GetTransferService}.
 */
@Service
@RequiredArgsConstructor
public class GetTransferServiceImpl implements GetTransferService {
    private final TransferRepository transferRepository;

    public PageResponseDto<Transfer> getTransfers(final int maxRecords) throws ResourceNotFoundException {
        List<Transfer> transfers = Optional.of(transferRepository.findAll(Pageable.ofSize(maxRecords)).toList())
                .orElseThrow(() -> new ResourceNotFoundException(Transfer.class));
        return new PageResponseDto<>(transfers);
    }

    public Transfer getTransferById(final UUID transferId) throws ResourceNotFoundException {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException(Transfer.class, transferId));
    }

}
