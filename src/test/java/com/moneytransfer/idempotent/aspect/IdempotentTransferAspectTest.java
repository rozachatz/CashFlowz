package com.moneytransfer.idempotent.aspect;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Account;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.entity.TransactionRequest;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.enums.TransactionRequestStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.RequestConflictException;
import com.moneytransfer.idempotent.annotation.IdempotentTransferRequest;
import com.moneytransfer.service.RequestService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {IdempotentTransferAspect.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class IdempotentTransferAspectTest {

    @Autowired
    private IdempotentTransferAspect idempotentTransferAspect;

    @MockBean
    private RequestService requestService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    private Account sourceAccount, targetAccount;


    @BeforeEach
    public void setup() {
        sourceAccount = new Account(0, UUID.randomUUID(), "Name1", BigDecimal.TEN, Currency.EUR, LocalDateTime.now());
        targetAccount = new Account(0, UUID.randomUUID(), "Name2", BigDecimal.TEN, Currency.USD, LocalDateTime.now());
    }


    /**
     * Tests the {@link IdempotentTransferAspect#handleIdempotentTransferRequest(ProceedingJoinPoint, IdempotentTransferRequest, NewTransferDto)}
     * method to ensure that the process of completing a successful transfer request results in the expected interactions and outcome.
     *
     * <p>This test verifies that:
     * <ul>
     *   <li> A transaction request is created.</li>
     *   <li> The transaction request is then completed with all fields filled. The http status and infoMessage fields should indicate success
     *   and the appropriate Transaction object should be also persisted.</li>
     *   <li> The method under test returns the appropriate Transaction object.</li>
     * </ul>
     *
     * @throws Throwable if any exception is thrown during the money transfer method execution.
     */
    @Test
    void test_Completion_SuccessfulRequest() throws Throwable {
        BigDecimal amount = BigDecimal.ONE;
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS, null, null, null);
        when(requestService.createRequest(newTransferDto)).thenReturn(transactionRequest);
        Transaction transactionToBeReturned = new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, sourceAccount.getCurrency());
        when(proceedingJoinPoint.proceed()).thenReturn(transactionToBeReturned);
        Transaction transaction = idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto);
        Assertions.assertEquals(transaction, transactionToBeReturned);
        verify(requestService, times(1)).createRequest(eq(newTransferDto));
        verify(requestService, times(1)).completeRequest(eq(transactionRequest), eq(transaction), eq(HttpStatus.CREATED), eq(HttpStatus.CREATED.getReasonPhrase()));
    }

    /**
     * Tests the {@link IdempotentTransferAspect#handleIdempotentTransferRequest(ProceedingJoinPoint, IdempotentTransferRequest, NewTransferDto)}
     * method to ensure that the process of completing a failed transfer request results in the expected interactions and outcome.
     *
     * <p>This test verifies that:
     * <ul>
     *   <li>A transaction request is created.</li>
     *   <li>The transaction request is completed. For a failed transfer request, no Transaction object should be persisted.</li>
     *   <li>The http status and infoMessage fields of the request should be provided by the {@link MoneyTransferException} thrown.</li>
     * </ul>
     * </p>
     *
     * @throws Throwable if any exception is thrown during the money transfer method execution.
     */
    @Test
    void test_Completion_FailedRequest() throws Throwable {
        BigDecimal amount = BigDecimal.ONE;
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS, null, null, null);
        when(requestService.createRequest(newTransferDto)).thenReturn(transactionRequest);
        when(proceedingJoinPoint.proceed()).thenThrow(new MoneyTransferException("test"));
        MoneyTransferException exception = assertThrows(MoneyTransferException.class, () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto));
        verify(requestService, times(1)).createRequest(eq(newTransferDto));
        verify(requestService, times(1)).completeRequest(eq(transactionRequest), eq(null), eq(exception.getHttpStatus()), eq(exception.getMessage()));
    }

    /**
     * Tests the {@link IdempotentTransferAspect#handleIdempotentTransferRequest(ProceedingJoinPoint, IdempotentTransferRequest, NewTransferDto)}
     * method to verify that all fields of a completed {@link TransactionRequest} are considered in the idempotency validation.
     *
     * <p>This test ensures that: A subsequent transfer request with different amount but same requestId, account ids fields in the given {@link NewTransferDto}
     * results in a {@link RequestConflictException}</p>
     *
     * @throws Throwable if any exception is thrown during the money transfer method execution.
     */
    @Test
    void testIdempotency_NotValidRequest() throws Throwable {
        BigDecimal amount = BigDecimal.ONE;
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto1 = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto1.requestId(), newTransferDto1.amount(), newTransferDto1.sourceAccountId(),
                newTransferDto1.targetAccountId(), TransactionRequestStatus.IN_PROGRESS, null, null, null);
        when(requestService.createRequest(newTransferDto1)).thenReturn(transactionRequest);
        idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto1);
        NewTransferDto newTransferDto2 = new NewTransferDto(newTransferDto1.requestId(), newTransferDto1.sourceAccountId(), newTransferDto1.targetAccountId(),
                newTransferDto1.amount().add(BigDecimal.ONE));
        when(requestService.getRequest(requestId)).thenReturn(transactionRequest);
        assertThrows(RequestConflictException.class,
                () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto2));
    }

    /**
     * Tests the idempotent behavior of {@link IdempotentTransferAspect#handleIdempotentTransferRequest(ProceedingJoinPoint, IdempotentTransferRequest, NewTransferDto)}
     * method for the happy path in which the idempotent transfer request proceeds with the money transfer operation.
     *
     * <p>This test ensures that corresponding transfer request always gives the same result, i.e., an identical {@link Transaction} object.</p>
     *
     * @throws Throwable if any exception is thrown during the money transfer method execution.
     */
    @Test
    void testIdempotency_HappyPath() throws Throwable {
        BigDecimal amount = BigDecimal.ONE;
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS, null, null, null);
        when(requestService.createRequest(newTransferDto)).thenReturn(transactionRequest);
        when(proceedingJoinPoint.proceed()).thenReturn(new Transaction(UUID.randomUUID(), sourceAccount, targetAccount, amount, sourceAccount.getCurrency()));
        Transaction transaction1 = idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto);
        when(requestService.getRequest(requestId)).thenReturn(transactionRequest);
        Transaction transaction2 = idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto);
        Assertions.assertEquals(transaction1, transaction2);
    }

    /**
     * Tests the idempotent behavior of {@link IdempotentTransferAspect#handleIdempotentTransferRequest(ProceedingJoinPoint, IdempotentTransferRequest, NewTransferDto)}
     * method for a scenario where the idempotent transfer request does not proceed with the money transfer operation.
     *
     * <p>This test ensures that the corresponding transfer request always throws a {@link MoneyTransferException},
     * with the same message and http status fields.</p>
     *
     * @throws Throwable if any exception is thrown during the money transfer method execution
     */
    @Test
    void testIdempotency_NotHappyPath() throws Throwable {
        BigDecimal amount = sourceAccount.getBalance();
        UUID requestId = UUID.randomUUID();
        NewTransferDto newTransferDto = new NewTransferDto(requestId, sourceAccount.getAccountId(), targetAccount.getAccountId(), amount);
        TransactionRequest transactionRequest = new TransactionRequest(newTransferDto.requestId(), newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId(), TransactionRequestStatus.IN_PROGRESS, null, null, null);
        when(requestService.createRequest(newTransferDto)).thenReturn(transactionRequest);
        when(proceedingJoinPoint.proceed()).thenThrow(new MoneyTransferException("test"));
        MoneyTransferException exception1 = assertThrows(MoneyTransferException.class, () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto));
        when(requestService.getRequest(requestId)).thenReturn(transactionRequest);
        MoneyTransferException exception2 = assertThrows(MoneyTransferException.class, () -> idempotentTransferAspect.handleIdempotentTransferRequest(proceedingJoinPoint, null, newTransferDto));
        Assertions.assertEquals(exception1.getHttpStatus(), exception2.getHttpStatus());
        Assertions.assertEquals(exception1.getMessage(), exception2.getMessage());
    }
}
