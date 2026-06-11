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
public class ImportJobResponse {

    private Long id;
    private String fileName;
    private Long fileSize;
    private String status;
    private int rowsProcessed;
    private int rowsImported;
    private int errorsCount;
    private String bankFormat;
    private LocalDateTime createdAt;

}
