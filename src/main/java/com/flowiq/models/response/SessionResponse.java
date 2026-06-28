package com.flowiq.models.response;

import lombok.Data;

@Data
public class SessionResponse {
    private String id;
    private String device;
    private String ipAddress;
    private String lastActive;
    private boolean current;
}
