package com.moneytransactions.moneytransfer.controller;

import com.moneytransactions.moneytransfer.dto.TransferDTO;
import com.moneytransactions.moneytransfer.dto.TransferRequestDTO;
import com.moneytransactions.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransactions.moneytransfer.service.TransactionServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnExpression("!${myapp.controller.enabled:true}")
public class TransactionControllerImpl implements TransactionController {
    private final TransactionServiceImpl transactionServiceImpl;

    public TransactionControllerImpl(TransactionServiceImpl transactionServiceImpl) { //default: singleton scope

        this.transactionServiceImpl = transactionServiceImpl;
    }

    @PostMapping("/transactions/optimistic") //Endpoint
    public ResponseEntity<TransferDTO> createOptimisticTransaction(@RequestBody TransferRequestDTO transferRequestDTO) throws MoneyTransferException {
        return ResponseEntity.ok(transactionServiceImpl.transferFunds(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount(),
                "Optimistic"
        ));
    }

    @PostMapping("/transactions/pessimistic") //Endpoint
    public ResponseEntity<TransferDTO> createPessimisticTransaction(@RequestBody TransferRequestDTO transferRequestDTO) throws MoneyTransferException {
        return ResponseEntity.ok(transactionServiceImpl.transferFunds(
                transferRequestDTO.sourceAccountId(),
                transferRequestDTO.targetAccountId(),
                transferRequestDTO.amount(),
                "Pessimistic"
        ));
    }

}


//curl -X POST -H "Content-Type: application/json" -d "{ \"sourceAccountId\": 1, \"targetAccountId\": 2, \"amount\": "30.00"}" "http://localhost:8080/transferMoney"