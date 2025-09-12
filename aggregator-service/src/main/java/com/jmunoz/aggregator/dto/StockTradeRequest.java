package com.jmunoz.aggregator.dto;

import com.jmunoz.aggregator.domain.Ticker;
import com.jmunoz.aggregator.domain.TradeAction;

public record StockTradeRequest(Ticker ticker,
                                Integer price,
                                Integer quantity,
                                TradeAction action) {
}
