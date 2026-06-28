package com.flowiq.models.response;

import lombok.Data;

@Data
public class FopProfileResponse {
    private String taxSystem;
    private String fopGroup;
    private String activityType;
    private String registrationDate;
}
