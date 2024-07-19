package com.moneytransfer.service;

import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransferStatus;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetTransferServiceTest {
    @InjectMocks
    GetTransferServiceImpl getTransferService;
    @Mock
    private TransferRepository transferRepository;
    Account sourceAccount, targetAccount;
    List<Transfer> transfers;

    @BeforeEach
    public void before() {
        sourceAccount = new Account(0, UUID.randomUUID(), "test", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), "test", BigDecimal.ZERO, Currency.EUR, LocalDateTime.now());
        Transfer transfer1 = new Transfer(UUID.randomUUID(), sourceAccount, targetAccount, BigDecimal.ZERO, sourceAccount.getCurrency(), TransferStatus.FUNDS_TRANSFERRED);
        Transfer transfer2 = new Transfer(UUID.randomUUID(), sourceAccount, targetAccount, BigDecimal.ONE, sourceAccount.getCurrency(), TransferStatus.FUNDS_TRANSFERRED);
        transfers = List.of(transfer1, transfer2);
    }

    @Test
    void testGetTransfers() throws ResourceNotFoundException {
        int maxRecords = 5;
        doReturn(new PageImpl<>(transfers)).when(transferRepository).findAll(Pageable.ofSize(maxRecords));
        PageResponseDto<Transfer> result = getTransferService.getTransfers(maxRecords);
        assertEquals(transfers, result.content());
    }

    @Test
    void testGetTransferById_Success() throws ResourceNotFoundException {
        UUID transferId = UUID.randomUUID();
        Transfer transfer = transfers.get(0);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));
        Transfer result = getTransferService.getTransferById(transferId);
        assertEquals(transfer, result);
    }

    @Test
    void testGetTransferById_NotFound() {
        UUID transferId = UUID.randomUUID();
        when(transferRepository.findById(transferId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> getTransferService.getTransferById(transferId));
    }
}
