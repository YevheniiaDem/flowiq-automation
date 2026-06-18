package com.flowiq.agents.generator.schema;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class JsonSchemaDocument {
    String filePath;
    String title;
    String moduleHint;
    String resourceHint;
  @Singular
    List<SchemaFieldConstraint> fields;
  @Singular
    List<String> requiredFields;
}
