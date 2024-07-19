package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.PageResponseDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transfer;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.service.GetAccountService;
import com.moneytransfer.service.GetTransferService;
import com.moneytransfer.service.MoneyTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements the {@link MoneyTransferAPIController}
 */
@RestController
@RequiredArgsConstructor
public class MoneyTransferAPIControllerImpl
        implements MoneyTransferAPIController {
    private final GetAccountService getAccountService;
    private final GetTransferService getTransferService;
    private final MoneyTransferService moneyTransferService;

    @PostMapping("/transfer")
    public ResponseEntity<GetTransferDto> transferRequest(@RequestBody NewTransferDto newTransferDto) throws MoneyTransferException {
        Transfer transfer = moneyTransferService.transferSerializable(newTransferDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GetTransferDto(transfer.getTransferId(), transfer.getSourceAccount().getAccountId(),
                        transfer.getTargetAccount().getAccountId(), transfer.getAmount(),
                        transfer.getCurrency()));
    }

    @GetMapping("/transfers/{maxRecords}")
    public ResponseEntity<List<GetTransferDto>> getTransfers(int maxRecords) throws ResourceNotFoundException {
        PageResponseDto<Transfer> transactions = getTransferService.getTransfers(maxRecords);
        return ResponseEntity.ok(transactions.content().stream()
                .map(transaction -> new GetTransferDto(transaction.getTransferId(),
                        transaction.getSourceAccount().getAccountId(), transaction.getTargetAccount().getAccountId(),
                        transaction.getAmount(), transaction.getCurrency())).collect(Collectors.toList()));
    }

    @GetMapping("/transfer/{id}")
    public ResponseEntity<GetTransferDto> getTransferById(@PathVariable UUID id) throws ResourceNotFoundException {
        Transfer transfer = getTransferService.getTransferById(id);
        return ResponseEntity.ok(
                new GetTransferDto(transfer.getTransferId(), transfer.getSourceAccount().getAccountId(),
                        transfer.getTargetAccount().getAccountId(), transfer.getAmount(),
                        transfer.getCurrency()));
    }

    @GetMapping("/accounts/{maxRecords}")
    public ResponseEntity<List<GetAccountDto>> getAccounts(@PathVariable int maxRecords) {
        PageResponseDto<Account> accounts = getAccountService.getAccounts(maxRecords);
        return ResponseEntity.ok(accounts.content().stream()
                .map(account -> new GetAccountDto(account.getAccountId(), account.getBalance(), account.getCurrency()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<GetAccountDto> getAccountById(@PathVariable UUID id) throws ResourceNotFoundException {
        Account account = getAccountService.getAccountById(id);
        return ResponseEntity.ok(new GetAccountDto(account.getAccountId(), account.getBalance(), account.getCurrency()));
    }
}
