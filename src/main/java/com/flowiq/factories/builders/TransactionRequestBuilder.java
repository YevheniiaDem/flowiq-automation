package com.flowiq.factories.builders;

import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.request.UpdateTransactionRequest;
import com.flowiq.utils.DateUtils;
import com.flowiq.utils.RandomDataGenerator;

import java.math.BigDecimal;

public final class TransactionRequestBuilder {

    private final CreateTransactionRequest request;

    private TransactionRequestBuilder(CreateTransactionRequest request) {
        this.request = request;
    }

    public static TransactionRequestBuilder expense() {
        CreateTransactionRequest request = TestDataFactory.validTransactionRequest();
        request.setType(CreateTransactionRequest.TransactionTypeDto.EXPENSE);
        request.setCategory("Office");
        return new TransactionRequestBuilder(request);
    }

    public static TransactionRequestBuilder income() {
        CreateTransactionRequest request = TestDataFactory.validTransactionRequest();
        request.setType(CreateTransactionRequest.TransactionTypeDto.INCOME);
        request.setCategory("Services");
        request.setAmount(new BigDecimal("250.00"));
        return new TransactionRequestBuilder(request);
    }

    public TransactionRequestBuilder amount(BigDecimal amount) {
        request.setAmount(amount);
        return this;
    }

    public TransactionRequestBuilder category(String category) {
        request.setCategory(category);
        return this;
    }

    public TransactionRequestBuilder description(String description) {
        request.setDescription(description);
        return this;
    }

    public TransactionRequestBuilder uniqueDescription() {
        request.setDescription("Auto test " + RandomDataGenerator.alphanumeric(8));
        return this;
    }

    public TransactionRequestBuilder today() {
        request.setTransactionDate(DateUtils.parseDate(DateUtils.today()));
        return this;
    }

    public CreateTransactionRequest build() {
        return request;
    }

    public UpdateTransactionRequest toUpdateRequest() {
        UpdateTransactionRequest update = new UpdateTransactionRequest();
        update.setType(request.getType());
        update.setAmount(request.getAmount());
        update.setCategory(request.getCategory());
        update.setDescription(request.getDescription());
        update.setTransactionDate(request.getTransactionDate());
        return update;
    }
}
