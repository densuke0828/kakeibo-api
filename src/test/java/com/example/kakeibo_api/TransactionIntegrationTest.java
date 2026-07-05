package com.example.kakeibo_api;

import com.example.kakeibo_api.dto.TransactionRequest;
import com.example.kakeibo_api.entity.Transaction;
import com.example.kakeibo_api.enums.TransactionType;
import com.example.kakeibo_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransactionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void POST_transactions_取引が登録される() throws Exception {
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 1000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "食費");
        ReflectionTestUtils.setField(request, "memo", "鶏肉");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 3, 1));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("食費"));

        assertThat(transactionRepository.count()).isEqualTo(1);
    }

    @Test
    void GET_transactions_指定した種別の取引を取得() throws Exception {
        transactionRepository.save(Transaction.create(
                        1000, TransactionType.EXPENSE, "食費",
                        "鶏肉", LocalDate.of(2026, 3, 1)));
        transactionRepository.save(Transaction.create(
                        20000, TransactionType.INCOME, "給料",
                        "ボーナス", LocalDate.of(2026, 3, 1)));

        mockMvc.perform(get("/transactions")
                .param("transactionType", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"));
    }

    @Test
    void GET_transactions_登録取引を全件取得() throws Exception {
        transactionRepository.save(Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.of(2026, 3, 1)));
        transactionRepository.save(Transaction.create(
                20000, TransactionType.INCOME, "給料",
                "ボーナス", LocalDate.of(2026, 3, 1)));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("食費"))
                .andExpect(jsonPath("$[1].category").value("給料"));
    }

    @Test
    void GET_transactions_summary_指定した年月の集計結果を取得() throws Exception {
        transactionRepository.save(Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.of(2026, 3, 1)));
        transactionRepository.save(Transaction.create(
                5000, TransactionType.EXPENSE, "交通費",
                "タクシー", LocalDate.of(2026, 4, 1)));
        transactionRepository.save(Transaction.create(
                20000, TransactionType.INCOME, "給料",
                "ボーナス", LocalDate.of(2026, 3, 1)));

        mockMvc.perform(get("/transactions/summary")
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.totalIncome").value(20000))
                .andExpect(jsonPath("$.totalExpense").value(1000))
                .andExpect(jsonPath("$.balance").value(19000));
    }

    @Test
    void PUT_transactions_id_取引を更新する() throws Exception {
        Transaction savedTransaction = transactionRepository.save(Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.of(2026, 3, 1)));
        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "amount", 5000);
        ReflectionTestUtils.setField(request, "transactionType", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "category", "交通費");
        ReflectionTestUtils.setField(request, "memo", "タクシー");
        ReflectionTestUtils.setField(request, "date", LocalDate.of(2026, 3, 1));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/transactions/" + savedTransaction.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("交通費"));

        Transaction updated = transactionRepository.findById(savedTransaction.getId()).orElseThrow();
        assertThat(updated.getAmount()).isEqualTo(5000);
        assertThat(updated.getCategory()).isEqualTo("交通費");
    }

    @Test
    void DELETE_transactions_id_取引が削除される() throws Exception {
        Transaction savedTransaction = transactionRepository.save(Transaction.create(
                1000, TransactionType.EXPENSE, "食費",
                "鶏肉", LocalDate.of(2026, 3, 1)));

        mockMvc.perform(delete("/transactions/" + savedTransaction.getId()))
                .andExpect(status().isNoContent());

        assertThat(transactionRepository.findById(savedTransaction.getId())).isEmpty();
    }
}
