package com.jmunoz.aggregator.service;

import com.jmunoz.aggregator.client.CustomerServiceClient;
import com.jmunoz.aggregator.client.StockServiceClient;
import com.jmunoz.aggregator.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerPortfolioService {

    private final StockServiceClient stockServiceClient;
    private final CustomerServiceClient customerServiceClient;

    public CustomerPortfolioService(StockServiceClient stockServiceClient, CustomerServiceClient customerServiceClient) {
        this.stockServiceClient = stockServiceClient;
        this.customerServiceClient = customerServiceClient;
    }

    public Mono<CustomerInformation> getCustomerInformation(Integer customerId) {
        return this.customerServiceClient.getCustomerInformation(customerId);
    }

    public Mono<StockTradeResponse> trade(Integer customerId, TradeRequest request) {
        // Obtenemos el nuevo precio del ticker.
        // Luego construimos StockTradeRequest.
        // Hacemos el trade con el.
        return this.stockServiceClient.getStockPrice(request.ticker())
                .map(StockPriceResponse::price)
                .map(price -> this.toStockTradeRequest(request, price))
                .flatMap(req -> this.customerServiceClient.trade(customerId, req));

    }

    // Esto podría estar en una clase de mapper, pero como solo necesitamos hacer un mapeo
    // en toda la aplicación, lo creamos aquí como méto-do privado.
    private StockTradeRequest toStockTradeRequest(TradeRequest request, Integer price) {
        return new StockTradeRequest(
                request.ticker(),
                price,
                request.quantity(),
                request.action()
        );
    }
}
