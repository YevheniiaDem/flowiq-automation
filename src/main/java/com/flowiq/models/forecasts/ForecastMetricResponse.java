package com.flowiq.models.forecasts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastMetricResponse {

    private List<ForecastDataPointDto> historical;
    private List<ForecastDataPointDto> projected;
    private double trendPercent;
    private List<ForecastHorizonDto> horizons;
}
