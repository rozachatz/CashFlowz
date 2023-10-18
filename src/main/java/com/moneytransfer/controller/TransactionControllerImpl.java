package com.moneytransfer.controller;

import com.moneytransfer.dto.GetAccountDto;
import com.moneytransfer.dto.GetTransferDto;
import com.moneytransfer.dto.TransferRequestDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.service.TransactionRequestService;
import com.moneytransfer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {
    private final TransactionService transactionService;
    private final TransactionRequestService transactionRequestService;

    /**
     * Get all transactions with amount withing range
     * @param minAmount
     * @param maxAmount
     * @return A list of GetTransferDto Objects
     * @throws ResourceNotFoundException
     */
    @Cacheable
    @GetMapping("/transactions/{minAmount}/{maxAmount}")
    public ResponseEntity<List<GetTransferDto>> getTransactionsWithinRange(
            @PathVariable BigDecimal minAmount,
            @PathVariable BigDecimal maxAmount) throws ResourceNotFoundException {
        List<Transaction> transactions = transactionService.getTransactionByAmountBetween(minAmount, maxAmount);
        return ResponseEntity.ok(
                transactions.stream()
                        .map(transaction -> new GetTransferDto(
                                transaction.getId(),
                                transaction.getSourceAccount().getId(),
                                transaction.getTargetAccount().getId(),
                                transaction.getAmount()))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Return all accounts with #results<limit
     * @param limit
     * @return List of Account Objects
     */
    @Cacheable
    @GetMapping("/accounts/{limit}") //TODO: Return Account DTO
    public ResponseEntity<Page<Account>> getAccountsWithLimit(@PathVariable int limit) {
        Page<Account> entities = transactionService.getAccountsWithLimit(limit);
        return ResponseEntity.ok(entities);
    }

    /**
     * Get Account by id
     * @param id
     * @return associated Account
     * @throws ResourceNotFoundException
     */
    @GetMapping("/account/{id}")
    public ResponseEntity<GetAccountDto> getAccountById(@PathVariable UUID id) throws ResourceNotFoundException {
        Account account = transactionService.getAccountById(id);
        return ResponseEntity.ok(new GetAccountDto(
                account.getId(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt()));
    }

    /**
     * Get Transaction by id
     * @param id
     * @return GetTransferDto for the associated Transaction
     * @throws ResourceNotFoundException
     */
    @GetMapping("/transfer/{id}")
    public ResponseEntity<GetTransferDto> getTransactionById(@PathVariable UUID id) throws ResourceNotFoundException {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(new GetTransferDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getTargetAccount().getId(),
                transaction.getAmount()));
    }



    /**
     * New transfer request, optimistic locking
     * @param transferRequestDTO
     * @return GetTransferDto for the new Transaction
     * @throws MoneyTransferException
     */

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

    /**
     * New transfer request, pessimistic locking
     * @param transferRequestDTO
     * @return GetTransferDto for the new Transaction
     * @throws MoneyTransferException
     */
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


    /**
     * New IDEMPOTENT tranfer request //TODO: Fix, buggy
     * @param transferRequestDto
     * @param requestId
     * @return
     * @throws MoneyTransferException
     */
    @PostMapping("/transfer/{requestId}")
    public ResponseEntity<GetTransferDto> transfer(@RequestBody TransferRequestDto transferRequestDto, @PathVariable UUID requestId) throws MoneyTransferException {
        Transaction transaction = transactionRequestService.processRequest(
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


}