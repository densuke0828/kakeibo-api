package com.example.kakeibo_api.service;

import com.example.kakeibo_api.dto.MonthlySummaryResponse;
import com.example.kakeibo_api.dto.TransactionRequest;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import com.example.kakeibo_api.exception.TransactionNotFoundException;
import com.example.kakeibo_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_正常系_登録した取引が返る() {
        TransactionRequest request = new TransactionRequest();
        LocalDate date = LocalDate.now();
        ReflectionTestUtils.setField(request, "amount", 1000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "食費");
        ReflectionTestUtils.setField(request, "date", date);
        Transaction transaction = Transaction.create(
                request.getAmount(), request.getTransactionType(), request.getCategory(),
                null, request.getDate());
        given(transactionRepository.save(any(Transaction.class))).willReturn(transaction);

        transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        then(transactionRepository).should().save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(1000);
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(captor.getValue().getCategory()).isEqualTo("食費");
        assertThat(captor.getValue().getDate()).isEqualTo(date);
    }

    @Test
    void findAll_正常系_登録済みの取引一覧が返る() {
        Transaction transaction1 = Transaction.create(
                200000, TransactionType.INCOME, "給料",
                "ボーナス", LocalDate.now());
        Transaction transaction2 = Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.now());
        given(transactionRepository.findAll()).willReturn(List.of(transaction1, transaction2));

        List<Transaction> result = transactionService.findAll();

        then(transactionRepository).should().findAll();
        assertThat(result)
                .hasSize(2)
                .extracting(Transaction::getTransactionType)
                .containsExactly(TransactionType.INCOME, TransactionType.EXPENSE);
    }

    @Test
    void findAll_正常系_空のリストが返る() {
        given(transactionRepository.findAll()).willReturn(List.of());

        List<Transaction> result = transactionService.findAll();

        then(transactionRepository).should().findAll();
        assertThat(result).isEmpty();
    }

    @Test
    void searchByTransactionType_正常系_指定した取引種別のリストが返る() {
        Transaction transaction1 = Transaction.create(
                100000, TransactionType.INCOME, "給料",
                "ボーナス", LocalDate.now());
        Transaction transaction2 = Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.now());
        Transaction transaction3 = Transaction.create(
                200000, TransactionType.INCOME, "給料",
                "N月分", LocalDate.now());
        given(transactionRepository.findByTransactionType(TransactionType.INCOME))
                .willReturn(List.of(transaction1, transaction3));

        List<Transaction> result =
                transactionService.searchByTransactionType(TransactionType.INCOME);

        then(transactionRepository).should().findByTransactionType(TransactionType.INCOME);
        assertThat(result)
                .hasSize(2)
                .extracting(Transaction::getTransactionType)
                .containsExactly(TransactionType.INCOME, TransactionType.INCOME);
    }

    @Test
    void searchByTransactionType_正常系_指定した取引種別が登録されてないなら空リストが返る() {
        given(transactionRepository.findByTransactionType(TransactionType.INCOME))
                .willReturn(List.of());

        List<Transaction> result =
                transactionService.searchByTransactionType(TransactionType.INCOME);

        then(transactionRepository).should().findByTransactionType(TransactionType.INCOME);
        assertThat(result).isEmpty();
    }

    @Test
    void getMonthlySummary_正常系_指定した年月の集計結果が返る() {
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                2026, 3, 10000L, 3000L, 7000L);
        given(transactionRepository.findMonthlySummary(2026, 3))
                .willReturn(Optional.of(response));

        MonthlySummaryResponse result = transactionService.getMonthlySummary(2026, 3);

        then(transactionRepository).should().findMonthlySummary(2026, 3);
        assertThat(result.getTotalIncome()).isEqualTo(10000L);
        assertThat(result.getTotalExpense()).isEqualTo(3000L);
        assertThat(result.getBalance()).isEqualTo(7000L);
    }

    @Test
    void getMonthlySummary_正常系_指定した年月の登録がなくても例外を投げない() {
        given(transactionRepository.findMonthlySummary(2026, 1))
                .willReturn(Optional.empty());

        MonthlySummaryResponse result = transactionService.getMonthlySummary(2026, 1);

        then(transactionRepository).should().findMonthlySummary(2026, 1);
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getMonth()).isEqualTo(1);
        assertThat(result.getTotalIncome()).isEqualTo(0L);
        assertThat(result.getTotalExpense()).isEqualTo(0L);
        assertThat(result.getBalance()).isEqualTo(0L);
    }

    @Test
    void updateTransaction_正常系_更新された取引が返る() {
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 2000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.INCOME);
        ReflectionTestUtils.setField(request, "category", "給料");
        ReflectionTestUtils.setField(request, "memo", "ボーナス");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 4, 1));
        Transaction foundTransaction = Transaction.create(
                1000, TransactionType.INCOME, "給料",
                "3月分", LocalDate.of(2026, 3, 1));
        given(transactionRepository.findById(1L)).willReturn(Optional.of(foundTransaction));

        Transaction result = transactionService.updateTransaction(1L, request);

        then(transactionRepository).should().findById(1L);
        assertThat(result.getAmount()).isEqualTo(2000);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getCategory()).isEqualTo("給料");
        assertThat(result.getMemo()).isEqualTo("ボーナス");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    void updateTransaction_異常系_TransactionNotFoundExceptionがスローされる() {
        given(transactionRepository.findById(1L)).willReturn(Optional.empty());
        
        assertThatThrownBy(() -> transactionService.updateTransaction(1L, new TransactionRequest()))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void deleteTransaction_正常系_取引が削除される() {
        Transaction foundTransaction = Transaction.create(
                1000, TransactionType.INCOME, "給料",
                "3月分", LocalDate.of(2026, 3, 1));
        given(transactionRepository.findById(1L)).willReturn(Optional.of(foundTransaction));

        transactionService.deleteTransaction(1L);

        then(transactionRepository).should().findById(1L);
        then(transactionRepository).should().delete(foundTransaction);
    }

    @Test
    void deleteTransaction_異常系_TransactionNotFoundExceptionがスローされる() {
        given(transactionRepository.findById(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.deleteTransaction(1L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

}
