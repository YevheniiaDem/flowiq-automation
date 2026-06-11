package com.flowiq.models.forecasts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FopLimitHorizonDto {

    private int months;
    private BigDecimal projectedAnnualIncome;
    private double projectedUsagePercent;
    private boolean limitExceeded;
}
