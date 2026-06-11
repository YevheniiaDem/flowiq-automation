package com.flowiq.models.forecasts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FopLimitForecastResponse {

    private int fopGroup;
    private String fopGroupLabel;
    private BigDecimal incomeLimit;
    private BigDecimal currentAnnualIncome;
    private double currentUsagePercent;
    private int monthsUntilLimitExceeded;
    private List<FopLimitHorizonDto> horizons;
}
