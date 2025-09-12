package com.jmunoz.aggregator.controller;

import com.jmunoz.aggregator.dto.CustomerInformation;
import com.jmunoz.aggregator.dto.StockTradeResponse;
import com.jmunoz.aggregator.dto.TradeRequest;
import com.jmunoz.aggregator.service.CustomerPortfolioService;
import com.jmunoz.aggregator.validator.RequestValidator;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("customers")
public class CustomerPortfolioController {

    private final CustomerPortfolioService customerPortfolioService;

    public CustomerPortfolioController(CustomerPortfolioService customerPortfolioService) {
        this.customerPortfolioService = customerPortfolioService;
    }

    @GetMapping("/{customerId}")
    public Mono<CustomerInformation> getCustomerInformation(@PathVariable Integer customerId) {
        return this.customerPortfolioService.getCustomerInformation(customerId);
    }

    @PostMapping("/{customerId}/trade")
    public Mono<StockTradeResponse> trade(@PathVariable Integer customerId, @RequestBody Mono<TradeRequest> mono) {
        // Validamos la entrada.
        // Con TradeRequest validado (req) hacemos el trade.
        return mono.transform(RequestValidator.validate())
                .flatMap(req -> this.customerPortfolioService.trade(customerId, req));
    }
}
