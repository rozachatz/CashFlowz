package com.cashflowz.moneytransfer.idempotent.aspect;

import com.cashflowz.moneytransfer.dto.NewTransferDto;
import com.cashflowz.moneytransfer.entity.Account;
import com.cashflowz.moneytransfer.entity.Transfer;
import com.cashflowz.moneytransfer.entity.TransferRequest;
import com.cashflowz.moneytransfer.enums.Currency;
import com.cashflowz.moneytransfer.enums.TransferRequestStatus;
import com.cashflowz.moneytransfer.exceptions.MoneyTransferException;
import com.cashflowz.moneytransfer.exceptions.RequestConflictException;
import com.cashflowz.moneytransfer.exceptions.ResourceNotFoundException;
import com.cashflowz.moneytransfer.idempotent.aspect.IdempotentTransferAspect;
import com.cashflowz.moneytransfer.service.TransferRequestService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotentTransferAspectTest {
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private KafkaTemplate<String,String > kafkaTemplate;

    @Mock
    TransferRequestService requestService;

    @InjectMocks
    private IdempotentTransferAspect idempotentTransferAspect;

    private final Account sourceAccount, targetAccount;
    private final NewTransferDto newTransferDto;

    IdempotentTransferAspectTest() {
        this.sourceAccount = new Account(0, UUID.randomUUID(), "Name1", BigDecimal.TEN, Currency.EUR, LocalDateTime.now());
        this.targetAccount = new Account(0, UUID.randomUUID(), "Name2", BigDecimal.TEN, Currency.USD, LocalDateTime.now());
        newTransferDto = new NewTransferDto(UUID.randomUUID(), sourceAccount.getAccountId(), targetAccount.getAccountId(), BigDecimal.ONE);
    }


    @BeforeEach
    public void setup() throws ResourceNotFoundException {
        doReturn(null).when(transactionManager).getTransaction(any());
        doNothing().when(transactionManager).commit(any());
        TransferRequest transferRequest = new TransferRequest(newTransferDto.transferRequestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransferRequestStatus.IN_PROGRESS, null, null, null);
        when(requestService.getTransferRequest(eq(newTransferDto.transferRequestId()))).thenReturn(transferRequest);
    }


    @Test
    void test_IdempotentBehavior_WrongPayload() throws Throwable {
        when(proceedingJoinPoint.proceed()).thenReturn(new Transfer(newTransferDto.transferRequestId(), sourceAccount, targetAccount, newTransferDto.amount(), sourceAccount.getCurrency()));
        idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto);
        NewTransferDto newTransferDto2 = new NewTransferDto(newTransferDto.transferRequestId(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), newTransferDto.amount().multiply(BigDecimal.TEN));
        assertThrows(RequestConflictException.class, () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto2));
    }


    @Test
    void test_IdempotentBehavior_HappyPath() throws Throwable {
        when(proceedingJoinPoint.proceed()).thenReturn(new Transfer(newTransferDto.transferRequestId(), sourceAccount, targetAccount, newTransferDto.amount(), sourceAccount.getCurrency()));
        Transfer transfer1 = idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto);
        Transfer transfer2 = idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto);
        Assertions.assertEquals(transfer1, transfer2);
    }


    @Test
    void test_IdempotentBehavior_BusinessError() throws Throwable {
        when(proceedingJoinPoint.proceed()).thenThrow(new MoneyTransferException("test"));
        MoneyTransferException exception1 = assertThrows(MoneyTransferException.class, () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto));
        MoneyTransferException exception2 = assertThrows(MoneyTransferException.class, () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto));
        Assertions.assertEquals(exception1.getHttpStatus(), exception2.getHttpStatus());
        Assertions.assertEquals(exception1.getMessage(), exception2.getMessage());
    }

}
