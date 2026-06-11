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
public class ForecastSnapshotResponse {

    private BigDecimal expectedRevenue;
    private BigDecimal expectedProfit;
    private BigDecimal taxForecast;
    private double revenueTrendPercent;
    private int forecastMonths;
}
