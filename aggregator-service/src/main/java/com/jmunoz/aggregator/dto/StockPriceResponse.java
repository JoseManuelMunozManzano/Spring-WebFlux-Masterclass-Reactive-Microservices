package com.jmunoz.aggregator.dto;

import com.jmunoz.aggregator.domain.Ticker;

public record StockPriceResponse(Ticker ticker,
                                 Integer price) {
}
