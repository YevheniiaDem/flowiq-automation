package com.flowiq.models.notifications;

import lombok.Data;

@Data
public class MarkNotificationReadRequest {
    private Boolean read = true;
}
