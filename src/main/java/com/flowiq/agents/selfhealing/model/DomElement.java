package com.flowiq.agents.selfhealing.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DomElement {
    String tagName;
    String testId;
    String ariaLabel;
    String role;
    String id;
    String cssClasses;
    String textContent;
    int sourceIndex;
}
