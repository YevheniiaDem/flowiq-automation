package com.flowiq.agents.regressionrisk.model;

import com.flowiq.agents.gap.model.GapSeverity;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AffectedTests {
  @Singular("smokeTest")
    List<String> smokeTests;
  @Singular("contractTest")
    List<String> contractTests;
  @Singular("regressionTest")
    List<String> regressionTests;
  @Singular("uiTest")
    List<String> uiTests;

    public int totalTestClasses() {
        return smokeTests.size() + contractTests.size() + regressionTests.size() + uiTests.size();
    }
}
