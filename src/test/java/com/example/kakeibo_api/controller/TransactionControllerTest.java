package com.example.kakeibo_api.controller;

import com.example.kakeibo_api.dto.MonthlySummaryResponse;
import com.example.kakeibo_api.dto.TransactionRequest;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import com.example.kakeibo_api.exception.TransactionNotFoundException;
import com.example.kakeibo_api.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionService transactionService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createTransaction_正常系_登録された取引が返る() throws Exception {
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 1000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "食費");
        ReflectionTestUtils.setField(request, "memo", "鶏肉");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 3, 1));
        Transaction createdTransaction = Transaction.create(
                request.getAmount(), request.getTransactionType(), request.getCategory(),
                request.getMemo(), request.getDate());
        String json = objectMapper.writeValueAsString(request);
        given(transactionService.createTransaction(any(TransactionRequest.class)))
                .willReturn(createdTransaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("食費"));
    }

    @Test
    void createTransaction_異常系_バリデーションエラーで400が返る() throws Exception{
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 0);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "食費");
        ReflectionTestUtils.setField(request, "memo", "鶏肉");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 3, 1));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchByTransactionType_正常系_TransactionTypeあり_絞り込み結果が返る() throws Exception {
        Transaction foundTransaction1 = Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.of(2026, 3, 1));
        Transaction foundTransaction2 = Transaction.create(
                5000, TransactionType.EXPENSE, "交通費",
                "タクシー", LocalDate.of(2026, 3, 1));
        Transaction foundTransaction3 = Transaction.create(
                200000, TransactionType.INCOME, "給料",
                "3月分", LocalDate.of(2026, 3, 1));
        given(transactionService.searchByTransactionType(TransactionType.EXPENSE))
                .willReturn(List.of(foundTransaction1, foundTransaction2));

        mockMvc.perform(get("/transactions").param("transactionType", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"));
    }

    @Test
    void searchByTransactionType_正常系_TransactionTypeなし_全件取得する() throws Exception {
        Transaction foundTransaction1 = Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.of(2026, 3, 1));
        Transaction foundTransaction2 = Transaction.create(
                200000, TransactionType.INCOME, "給料",
                "3月分", LocalDate.of(2026, 3, 1));
        given(transactionService.findAll()).willReturn(List.of(foundTransaction1, foundTransaction2));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("食費"))
                .andExpect(jsonPath("$[1].category").value("給料"));
    }

    @Test
    void getMonthlySummary_正常系_集計結果が返る() throws Exception{
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                2026, 3, 10000L, 3000L, 7000L);
        given(transactionService.getMonthlySummary(2026, 3)).willReturn(response);

        mockMvc.perform(get("/transactions/summary")
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.totalIncome").value(10000L))
                .andExpect(jsonPath("$.totalExpense").value(3000L))
                .andExpect(jsonPath("$.balance").value(7000L));
    }

    @Test
    void getMonthlySummary_正常系_指定した年月がなくても例外を投げない() throws Exception {
        MonthlySummaryResponse response = new MonthlySummaryResponse(
                2026, 4, 0L, 0L, 0L);
        given(transactionService.getMonthlySummary(2026, 4)).willReturn(response);

        mockMvc.perform(get("/transactions/summary")
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(4))
                .andExpect(jsonPath("$.totalIncome").value(0L))
                .andExpect(jsonPath("$.totalExpense").value(0L))
                .andExpect(jsonPath("$.balance").value(0L));
    }

    @Test
    void updateTransaction_正常系_更新された取引が返る() throws Exception {
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 5000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "交通費");
        ReflectionTestUtils.setField(request, "memo", "タクシー");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 3, 1));
        Transaction savedtransaction = Transaction.create(
                request.getAmount(), request.getTransactionType(), request.getCategory(),
                request.getMemo(), request.getDate());
        String json = objectMapper.writeValueAsString(request);
        given(transactionService.updateTransaction(anyLong(), any(TransactionRequest.class)))
                .willReturn(savedtransaction);

        mockMvc.perform(put("/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("交通費"));
    }

    @Test
    void updateTransaction_異常系_404が返る() throws Exception {
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 5000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "交通費");
        ReflectionTestUtils.setField(request, "memo", "タクシー");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 3, 1));
        String json = objectMapper.writeValueAsString(request);
        given(transactionService.updateTransaction(anyLong(), any(TransactionRequest.class)))
                .willThrow(new TransactionNotFoundException(1L));

        mockMvc.perform(put("/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_正常系_204が返る() throws Exception {
        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTransaction_異常系_404が返る() throws Exception {
        willThrow(new TransactionNotFoundException(1L))
                .given(transactionService)
                .deleteTransaction(1L);

        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isNotFound());
    }
}
