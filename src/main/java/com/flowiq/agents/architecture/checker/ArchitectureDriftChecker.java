package com.flowiq.agents.architecture.checker;

import com.flowiq.agents.architecture.inventory.ArchitectureContext;

import java.util.List;

public interface ArchitectureDriftChecker {

    List<com.flowiq.agents.architecture.model.ArchitectureDriftIssue> check(ArchitectureContext context);
}
