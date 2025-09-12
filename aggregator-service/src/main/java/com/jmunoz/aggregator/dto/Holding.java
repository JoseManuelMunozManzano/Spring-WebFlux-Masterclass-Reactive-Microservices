package com.jmunoz.aggregator.dto;

import com.jmunoz.aggregator.domain.Ticker;

public record Holding(Ticker ticker,
                      Integer quantity) {
}
