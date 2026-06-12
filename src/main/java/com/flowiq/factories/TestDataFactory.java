package com.flowiq.factories;

import com.flowiq.config.ConfigManager;
import com.flowiq.models.request.AIAccountantChatRequest;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.request.GenerateReportRequest;
import com.flowiq.models.request.LoginRequest;
import com.flowiq.models.request.RegisterRequest;
import com.flowiq.models.tasks.CreateTaskRequest;
import com.flowiq.utils.DateUtils;
import com.flowiq.utils.RandomDataGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static LoginRequest defaultLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail(ConfigManager.getConfig().testUserEmail());
        request.setPassword(ConfigManager.getConfig().testUserPassword());
        return request;
    }

    public static LoginRequest randomLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail(RandomDataGenerator.email());
        request.setPassword(RandomDataGenerator.password(12));
        return request;
    }

    public static LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    public static LoginRequest invalidLoginRequest() {
        return loginRequest("", "");
    }

    public static RegisterRequest randomRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(RandomDataGenerator.email());
        request.setPassword(RandomDataGenerator.password(12));
        request.setName(RandomDataGenerator.firstName() + " " + RandomDataGenerator.lastName());
        request.setCompany(RandomDataGenerator.companyName());
        return request;
    }

    public static RegisterRequest registerRequestWithEmail(String email) {
        RegisterRequest request = randomRegisterRequest();
        request.setEmail(email);
        return request;
    }

    public static CreateTransactionRequest validTransactionRequest() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(CreateTransactionRequest.TransactionTypeDto.EXPENSE);
        request.setAmount(new BigDecimal("100.50"));
        request.setCategory("Office");
        request.setDescription("Smoke test transaction");
        request.setTransactionDate(DateUtils.parseDate(DateUtils.today()));
        return request;
    }

    public static CreateTransactionRequest invalidTransactionRequest() {
        return new CreateTransactionRequest();
    }

    public static CreateTaskRequest validTaskRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Smoke task " + RandomDataGenerator.alphanumeric(6));
        request.setDescription("Created by API smoke test");
        request.setDueDate(DateUtils.parseDate(DateUtils.tomorrow()));
        return request;
    }

    public static CreateTaskRequest invalidTaskRequest() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("");
        return request;
    }

    public static GenerateReportRequest validReportRequest() {
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportType(GenerateReportRequest.ReportType.PROFIT_AND_LOSS);
        request.setFormat(GenerateReportRequest.Format.PDF);
        request.setPeriodPreset("THIS_MONTH");
        return request;
    }

    public static GenerateReportRequest invalidReportRequest() {
        return new GenerateReportRequest();
    }

    public static AIAccountantChatRequest validChatRequest() {
        AIAccountantChatRequest request = new AIAccountantChatRequest();
        request.setMessage("Які мої основні витрати цього місяця?");
        return request;
    }

    public static AIAccountantChatRequest invalidChatRequest() {
        AIAccountantChatRequest request = new AIAccountantChatRequest();
        request.setMessage("");
        return request;
    }

    public static File sampleImportCsv() {
        try (InputStream inputStream = TestDataFactory.class.getClassLoader()
                .getResourceAsStream("testdata/universal-transactions.csv")) {
            if (inputStream == null) {
                throw new IllegalStateException("testdata/universal-transactions.csv not found");
            }
            Path tempFile = Files.createTempFile("flowiq-import-", ".csv");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile.toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load sample CSV", e);
        }
    }

    public static File invalidImportFile() {
        try {
            Path tempFile = Files.createTempFile("flowiq-invalid-import-", ".txt");
            Files.writeString(tempFile, "not a csv file");
            tempFile.toFile().deleteOnExit();
            return tempFile.toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create invalid import file", e);
        }
    }

    public static File emptyImportCsv() {
        try {
            Path tempFile = Files.createTempFile("flowiq-empty-import-", ".csv");
            Files.writeString(tempFile, "");
            tempFile.toFile().deleteOnExit();
            return tempFile.toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create empty import file", e);
        }
    }
}
