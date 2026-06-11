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
public class ForecastSummaryResponse {

    private BigDecimal expectedRevenue;
    private BigDecimal expectedExpenses;
    private BigDecimal expectedProfit;
    private BigDecimal expectedTax;
    private double revenueTrendPercent;
    private double expenseTrendPercent;
    private double profitTrendPercent;
    private double fopLimitUsagePercent;
    private int monthsUntilFopLimit;
    private List<ForecastHorizonDto> revenueHorizons;
    private List<ForecastHorizonDto> profitHorizons;
    private List<ForecastInsightDto> insights;
    private List<ForecastWarningDto> warnings;
}
