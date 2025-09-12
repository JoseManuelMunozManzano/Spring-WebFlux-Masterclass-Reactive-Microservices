package com.jmunoz.aggregator.dto;

import com.jmunoz.aggregator.domain.Ticker;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PriceUpdate(Ticker ticker,
                          Integer price,
                          LocalDateTime time) {
}
