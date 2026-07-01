package com.example.kakeibo_api.service;

import com.example.kakeibo_api.dto.MonthlySummaryResponse;
import com.example.kakeibo_api.dto.TransactionRequest;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import com.example.kakeibo_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = false)
    public Transaction createTransaction(TransactionRequest request) {
        return transactionRepository.save(Transaction.create(
            request.getAmount(), request.getTransactionType(), request.getCategory(),
            request.getMemo(), request.getDate())
        );
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> searchByTransactionType(TransactionType transactionType) {
        return transactionRepository.findByTransactionType(transactionType);
    }

    public MonthlySummaryResponse getMonthlySummary(int year, int month) {
        return transactionRepository.findMonthlySummary(year, month)
                .orElse(new MonthlySummaryResponse(year, month, 0L, 0L, 0L));
    }

    @Transactional(readOnly = false)
    public Transaction updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("登録データがありません"));
        transaction.update(request.getAmount(), request.getTransactionType(),
                            request.getCategory(), request.getMemo(), request.getDate());
        return transaction;
    }

    @Transactional(readOnly = false)
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("登録データがありません"));
        transactionRepository.delete(transaction);
    }

}
