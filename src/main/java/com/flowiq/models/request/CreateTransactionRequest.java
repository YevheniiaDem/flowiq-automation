package com.flowiq.models.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {

    private TransactionTypeDto type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate transactionDate;

    public enum TransactionTypeDto {
        INCOME,
        EXPENSE
    }
}
