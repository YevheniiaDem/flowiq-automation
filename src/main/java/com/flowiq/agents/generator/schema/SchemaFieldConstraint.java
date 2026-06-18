package com.flowiq.agents.generator.schema;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SchemaFieldConstraint {
    String field;
    String type;
    boolean required;
    Integer minLength;
    Integer maxLength;
    Integer minimum;
    Integer maximum;
    String format;
  @Singular
    List<String> enumValues;
}
