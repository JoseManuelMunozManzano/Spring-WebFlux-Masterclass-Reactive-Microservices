package com.jmunoz.aggregator.client;

import com.jmunoz.aggregator.domain.Ticker;
import com.jmunoz.aggregator.dto.PriceUpdate;
import com.jmunoz.aggregator.dto.StockPriceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;

// Aunque esto debería ser un @Component de Spring, vamos a tener una clase config donde lo expondremos como un @Bean.
public class StockServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StockServiceClient.class);
    private final WebClient client;
    private Flux<PriceUpdate> flux;

    public StockServiceClient(WebClient client) {
        this.client = client;
    }

    // Si pasamos un ticker, obtendremos el precio.
    public Mono<StockPriceResponse> getStockPrice(Ticker ticker) {
        return this.client.get()
                .uri("/stock/{ticker}", ticker)
                .retrieve()
                .bodyToMono(StockPriceResponse.class);
    }

    // ¡Solo creamos el publisher una vez!
    public Flux<PriceUpdate> priceUpdateStream() {
        if (Objects.isNull(this.flux)) {
            this.flux = this.getPriceUpdate();
        }
        return this.flux;
    }

    // Obtenemos actualizaciones de precios.
    // Es un Hot Publisher (ver la clase Hot Price Stream en README.md)
    // Solo tendremos un publisher y muchos subscribers (nuestros usuarios).
    // El punto de hacer este publisher un Hot Publisher es que no queremos que otros invoquen este méto-do, ya
    // que solo puede crearse este publisher una sola vez.
    // Por eso este méto-do es private y solo se llama una vez desde el méto-do priceUpdateStream().
    private Flux<PriceUpdate> getPriceUpdate() {
        return this.client.get()
                .uri("/stock/price-stream")
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .bodyToFlux(PriceUpdate.class)
                // Si ocurre un error en la conexión, reintentamos para iniciar el stream de nuevo.
                .retryWhen(retry())
                // Hot Publisher (guardamos el valor anterior en caché para cuando un nuevo subscriber se subscriba)
                .cache(1);
    }

    private Retry retry() {
        return Retry.fixedDelay(100, Duration.ofSeconds(1))
                .doBeforeRetry(rs -> log.error("stock service price stream call failed. Retrying: {}", rs.failure().getMessage()));
    }
}
