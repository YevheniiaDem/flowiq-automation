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
public class ForecastDataPointDto {

    private String month;
    private BigDecimal amount;
    private boolean forecast;
}
