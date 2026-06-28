package com.flowiq.ci.flaky.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.ci.flaky.model.CiFlakyReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CiFlakyJsonReportWriter {

    private final ObjectMapper objectMapper;

    public CiFlakyJsonReportWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(CiFlakyReport report, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), report);
    }
}
