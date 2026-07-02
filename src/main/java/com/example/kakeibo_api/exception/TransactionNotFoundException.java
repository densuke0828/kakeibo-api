package com.example.kakeibo_api.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("ID: " + id + " が見つかりません");
    }

    public String getUserMessage() {
        return "登録データがありません";
    }
}
