package com.jmunoz.customerportfolio.dto;

import com.jmunoz.customerportfolio.domain.Ticker;

public record Holding(Ticker ticker,
                      Integer quantity) {
}
