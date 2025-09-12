package com.jmunoz.customerportfolio.dto;

import com.jmunoz.customerportfolio.domain.Ticker;
import com.jmunoz.customerportfolio.domain.TradeAction;

public record StockTradeRequest(Ticker ticker,
                                Integer price,
                                Integer quantity,
                                TradeAction action) {

    public Integer totalPrice() {
        return price * quantity;
    }
}
