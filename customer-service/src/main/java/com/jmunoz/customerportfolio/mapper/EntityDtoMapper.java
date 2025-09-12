package com.jmunoz.customerportfolio.mapper;

import com.jmunoz.customerportfolio.domain.Ticker;
import com.jmunoz.customerportfolio.dto.CustomerInformation;
import com.jmunoz.customerportfolio.dto.Holding;
import com.jmunoz.customerportfolio.dto.StockTradeRequest;
import com.jmunoz.customerportfolio.dto.StockTradeResponse;
import com.jmunoz.customerportfolio.entity.Customer;
import com.jmunoz.customerportfolio.entity.PortfolioItem;

import java.util.List;

public class EntityDtoMapper {

    public static CustomerInformation toCustomerInformation(Customer customer, List<PortfolioItem> items) {
        var holdings = items.stream()
                .map(i -> new Holding(i.getTicker(), i.getQuantity()))
                .toList();
        return new CustomerInformation(
                customer.getId(),
                customer.getName(),
                customer.getBalance(),
                holdings
        );
    }

    public static PortfolioItem toPortfolioItem(Integer customerId, Ticker ticker) {
        var portfolioItem = new PortfolioItem();
        portfolioItem.setCustomerId(customerId);
        portfolioItem.setTicker(ticker);
        portfolioItem.setQuantity(0);
        return portfolioItem;
    }

    public static StockTradeResponse toStockTradeResponse(StockTradeRequest request, Integer customerId, Integer balance) {
        return new StockTradeResponse(
                customerId,
                request.ticker(),
                request.price(),
                request.quantity(),
                request.action(),
                request.totalPrice(),
                balance
        );
    }
}
