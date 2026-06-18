package com.flowiq.agents.maintenance.scanner;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.maintenance.model.ScannedDto;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.model.ScannedSchema;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class MaintenanceContext {
    @Singular("testClass")
    List<ScannedTestClass> testClasses;
    @Singular("pageObject")
    List<ScannedPageObject> pageObjects;
    @Singular("dto")
    List<ScannedDto> dtos;
    @Singular("schema")
    List<ScannedSchema> schemas;
    @Singular("openApiEndpoint")
    Set<String> openApiEndpoints;
    @Singular("allureRecord")
    List<TestExecutionRecord> allureRecords;
    String combinedMainAndTestSources;
    String dataSourcesSummary;
}
