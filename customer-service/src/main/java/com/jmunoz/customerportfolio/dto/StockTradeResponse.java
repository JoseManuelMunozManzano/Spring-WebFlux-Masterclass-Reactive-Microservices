package com.jmunoz.customerportfolio.dto;

import com.jmunoz.customerportfolio.domain.Ticker;
import com.jmunoz.customerportfolio.domain.TradeAction;

public record StockTradeResponse(Integer customerId,
                                 Ticker ticker,
                                 Integer price,
                                 Integer quantity,
                                 TradeAction action,
                                 Integer totalPrice,
                                 Integer balance) {
}
