package com.example.kakeibo_api.repository;

import com.example.kakeibo_api.dto.MonthlySummaryResponse;
import com.example.kakeibo_api.dto.TransactionResponse;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class TransactionRepositoryTest {
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findByTransactionType_指定した取引種別のリストが返る() {
        transactionRepository.save(Transaction.create(
                1000, TransactionType.EXPENSE,
                "食費", "鶏肉", LocalDate.now()));
        transactionRepository.save(Transaction.create(
                200000, TransactionType.INCOME,
                "給料", "6月分", LocalDate.now()));

        List<Transaction> result =
                transactionRepository.findByTransactionType(TransactionType.EXPENSE);
        assertThat(result)
                .hasSize(1)
                .extracting(Transaction::getTransactionType)
                .containsExactly(TransactionType.EXPENSE);
    }

    @Test
    void findByTransactionType_該当する取引種別のデータがなければ空リストが返る() {
        transactionRepository.save(Transaction.create(
                1000, TransactionType.EXPENSE,
                "食費", "鶏肉", LocalDate.now()));
        transactionRepository.save(Transaction.create(
                5000, TransactionType.EXPENSE,
                "交通費", "タクシー", LocalDate.now()));

        List<Transaction> result =
                transactionRepository.findByTransactionType(TransactionType.INCOME);
        assertThat(result).isEmpty();
    }

    @Test
    void findMonthlySummary_集計値が正しい() {
        transactionRepository.save(Transaction.create(
                3000, TransactionType.EXPENSE,
                "食費", "鶏肉", LocalDate.of(2025, 6, 1)));
        transactionRepository.save(Transaction.create(
                2000, TransactionType.EXPENSE,
                "交通費", "タクシー", LocalDate.of(2025, 6, 1)));
        transactionRepository.save(Transaction.create(
                50000, TransactionType.INCOME,
                "給料", "ボーナス", LocalDate.of(2025, 6, 15)));
        transactionRepository.save(Transaction.create(
                200000, TransactionType.INCOME,
                "給料", "8月分", LocalDate.of(2025, 8, 27)));
        Optional<MonthlySummaryResponse> result =
                transactionRepository.findMonthlySummary(2025, 6);
        assertThat(result).isPresent().hasValueSatisfying(summary -> {
            assertThat(summary.getTotalIncome()).isEqualTo(50000L);
            assertThat(summary.getTotalExpense()).isEqualTo(5000L);
            assertThat(summary.getBalance()).isEqualTo(45000L);
        });
    }

    @Test
    void findMonthlySummary_該当月のデータがなければ空で返す() {
        transactionRepository.save(Transaction.create(
                3000, TransactionType.EXPENSE,
                "食費", "鶏肉", LocalDate.of(2025, 6, 1)));
        Optional<MonthlySummaryResponse> result =
                transactionRepository.findMonthlySummary(2026, 2);
        assertThat(result).isEmpty();
    }

}
