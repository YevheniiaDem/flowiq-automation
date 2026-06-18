package com.flowiq.agents.architecture.inventory;

import lombok.Value;

import java.util.Set;

@Value
class TestInventory {
    Set<String> contractTestClasses;
    Set<String> regressionTestClasses;
    Set<String> smokeTestClasses;
    Set<String> uiTestClasses;
}
