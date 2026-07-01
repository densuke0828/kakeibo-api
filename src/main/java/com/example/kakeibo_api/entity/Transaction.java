package com.example.kakeibo_api.entity;

import com.example.kakeibo_api.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "memo", length = 200)
    private String memo;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Transaction create(
            Integer amount, TransactionType transactionType, String category,
            String memo, LocalDate date) {
        return Transaction.builder()
                .amount(amount)
                .transactionType(transactionType)
                .category(category)
                .memo(memo)
                .date(date)
                .build();
    }

    public void update(Integer amount, TransactionType transactionType,
                       String category, String memo, LocalDate date) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.category = category;
        this.memo = memo;
        this.date = date;
    }
}
