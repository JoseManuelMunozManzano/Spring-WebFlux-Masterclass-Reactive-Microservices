package com.jmunoz.aggregator.dto;

import com.jmunoz.aggregator.domain.Ticker;
import com.jmunoz.aggregator.domain.TradeAction;

public record StockTradeResponse(Integer customerId,
                                 Ticker ticker,
                                 Integer price,
                                 Integer quantity,
                                 TradeAction action,
                                 Integer totalPrice,
                                 Integer balance) {
}
