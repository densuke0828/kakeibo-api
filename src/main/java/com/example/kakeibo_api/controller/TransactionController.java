package com.example.kakeibo_api.controller;

import com.example.kakeibo_api.dto.MonthlySummaryResponse;
import com.example.kakeibo_api.dto.TransactionRequest;
import com.example.kakeibo_api.dto.TransactionResponse;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import com.example.kakeibo_api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Validated @RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(transaction));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> searchByTransactionType(
            @RequestParam(required = false)TransactionType transactionType) {
        List<Transaction> transactions =
                transactionType != null ? transactionService.searchByTransactionType(transactionType) : transactionService.findAll();
        List<TransactionResponse> responses = transactions
                .stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(year, month));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id, @Validated @RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

}
