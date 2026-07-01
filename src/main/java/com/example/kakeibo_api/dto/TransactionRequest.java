package com.example.kakeibo_api.dto;

import com.example.kakeibo_api.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TransactionRequest {
    @NotNull(message = "金額は必須です")
    @Min(value = 1, message = "金額は1円以上で入力してください")
    private Integer amount;

    @NotNull(message = "取引種別は必須です")
    private TransactionType transactionType;

    @NotBlank(message = "カテゴリは必須です")
    @Size(max = 20, message = "カテゴリは20文字以内で入力してください")
    private String category;

    @Size(max = 200, message = "メモは200文字以内で入力してください")
    private String memo;

    @NotNull(message = "日付は必須です")
    private LocalDate date;
}
