package com.example.kakeibo_api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    INCOME("収入"),
    EXPENSE("支出");

    private final String displayName;
}
