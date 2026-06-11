package com.flowiq.models.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryResponse {
    private long total;
    private long unread;
    private long critical;
    private long warnings;
    private long success;
    private long thisMonth;
}
