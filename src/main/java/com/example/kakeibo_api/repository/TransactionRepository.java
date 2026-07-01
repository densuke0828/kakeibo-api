package com.example.kakeibo_api.repository;

import com.example.kakeibo_api.dto.MonthlySummaryResponse;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByTransactionType(TransactionType transactionType);

    @Query("""
            SELECT new com.example.kakeibo_api.dto.MonthlySummaryResponse(
                YEAR(t.date),
                MONTH(t.date),
                SUM(CASE WHEN t.transactionType = 'INCOME' THEN t.amount ELSE 0 END),
                SUM(CASE WHEN t.transactionType = 'EXPENSE' THEN t.amount ELSE 0 END),
                SUM(CASE WHEN t.transactionType = 'INCOME' THEN t.amount ELSE -t.amount END))
            FROM Transaction t
            WHERE YEAR(t.date) = :year AND MONTH(t.date) = :month
            GROUP BY YEAR(t.date), MONTH(t.date)
            """)
    Optional<MonthlySummaryResponse> findMonthlySummary(@Param("year") int year, @Param("month") int month);
}
