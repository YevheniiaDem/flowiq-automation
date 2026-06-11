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
public class TaxForecastResponse {

    private BigDecimal currentTaxBurden;
    private BigDecimal annualTaxForecast;
    private double trendPercent;
    private int fopGroup;
    private List<ForecastHorizonDto> horizons;
    private List<TaxForecastCardDto> cards;
}
