package com.flowiq.agents.architecture.inventory;

import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
class DocumentationInventory {
    List<ApiEndpointRef> endpoints;
    Set<String> modules;
}
