package com.flowiq.api.integration.support;

import com.flowiq.models.response.ImportJobResponse;
import com.flowiq.services.ImportService;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public final class ImportIntegrationSupport {

    private ImportIntegrationSupport() {
    }

    public static ImportJobResponse awaitCompletion(ImportService importService, long jobId) {
        return await()
                .atMost(90, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> importService.getById(jobId),
                        job -> !"PENDING".equals(job.getStatus()) && !"PROCESSING".equals(job.getStatus()));
    }
}
