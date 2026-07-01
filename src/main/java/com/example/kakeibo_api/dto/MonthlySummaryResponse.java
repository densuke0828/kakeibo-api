package com.example.kakeibo_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlySummaryResponse {
    private Integer year;
    private Integer month;
    private Long totalIncome;
    private Long totalExpense;
    private Long balance;
}
