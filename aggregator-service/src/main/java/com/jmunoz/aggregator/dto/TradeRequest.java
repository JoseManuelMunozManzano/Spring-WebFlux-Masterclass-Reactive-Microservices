package com.jmunoz.aggregator.dto;

import com.jmunoz.aggregator.domain.Ticker;
import com.jmunoz.aggregator.domain.TradeAction;

public record TradeRequest(Ticker ticker,
                           TradeAction action,
                           Integer quantity) {
}
