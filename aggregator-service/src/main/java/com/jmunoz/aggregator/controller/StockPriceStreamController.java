package com.jmunoz.aggregator.controller;

import com.jmunoz.aggregator.client.StockServiceClient;
import com.jmunoz.aggregator.dto.PriceUpdate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("stock")
public class StockPriceStreamController {

    // Normalmente, crearíamos una clase de servicio que sería la que inyectaríamos aquí.
    // Y ese servicio inyectaría el client y lo llamaría.
    // Esto es lo que se ha hecho en CustomerPortfolioController.
    // Pero como no se hace apenas nada, no veo la necesidad de crear una clase de servicio.
    private final StockServiceClient stockServiceClient;

    public StockPriceStreamController(StockServiceClient stockServiceClient) {
        this.stockServiceClient = stockServiceClient;
    }

    @GetMapping(value = "/price-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PriceUpdate> priceUpdateStream() {
        return this.stockServiceClient.priceUpdateStream();
    }
}
