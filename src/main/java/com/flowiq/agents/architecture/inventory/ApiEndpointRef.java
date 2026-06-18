package com.flowiq.agents.architecture.inventory;

import lombok.Value;

@Value
public class ApiEndpointRef {
    String method;
    String path;
    String source;

    public String key() {
        return method.toUpperCase() + " " + EndpointNormalizer.normalize(path);
    }
}
