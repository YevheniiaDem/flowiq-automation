package com.flowiq.models.response;

import com.flowiq.models.request.CreateTransactionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private CreateTransactionRequest.TransactionTypeDto type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate transactionDate;
    private boolean autoCategorized;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
