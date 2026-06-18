package com.flowiq.agents.traceability.docs;

import com.flowiq.agents.traceability.model.BusinessFeature;

import java.util.List;

public interface DocFeatureExtractor {

    String docFileName();

    List<BusinessFeature> extract(String markdown);
}
