package com.flowiq.models.response;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportStatsResponse {

    private long importedFiles;
    private long importedTransactions;
    private LocalDateTime lastImport;
    private double successRate;
}
