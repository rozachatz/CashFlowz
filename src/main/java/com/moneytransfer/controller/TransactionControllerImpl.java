package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.TransferRequestDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {
    private final TransactionService transactionService;

    @Cacheable("transactions")
    @GetMapping("/transfer/{id}")
    public ResponseEntity<GetTransferDto> getTransactionById(@PathVariable UUID id) throws ResourceNotFoundException {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(new GetTransferDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getAmount()));
    }
    @Cacheable("accounts")
    @GetMapping("/account/{id}")
    public ResponseEntity<GetAccountDto> getAccountById(@PathVariable UUID id) throws ResourceNotFoundException {
        Account account = transactionService.getAccountById(id);
        return ResponseEntity.ok(new GetAccountDto(
                account.getId(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt()));
    }

    @PostMapping("/transfer/{requestId}")
    public ResponseEntity<GetTransferDto> transfer(@RequestBody TransferRequestDto transferRequestDto, @PathVariable UUID requestId) throws MoneyTransferException {
        Transaction transaction = transactionService.processRequest(
                transferRequestDto.sourceAccountId(),
                transferRequestDto.targetAccountId(),
                transferRequestDto.amount(),
                requestId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }

    @PostMapping("/transfer/optimistic")
    public ResponseEntity<GetTransferDto> transferOptimistic(@RequestBody TransferRequestDto transferRequestDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferOptimistic(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }

    @PostMapping("/transfer/pessimistic")
    public ResponseEntity<GetTransferDto> transferPessimistic(@RequestBody TransferRequestDto transferRequestDTO) throws MoneyTransferException {
        Transaction transaction = transactionService.transferPessimistic(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GetTransferDto(
                        transaction.getId(),
                        transaction.getSourceAccount().getId(),
                        transaction.getTargetAccount().getId(),
                        transaction.getAmount()));
    }
}