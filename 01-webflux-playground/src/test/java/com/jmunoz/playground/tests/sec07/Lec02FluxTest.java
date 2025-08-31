package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.Duration;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec02/product/stream`.
public class Lec02FluxTest extends AbstractWebClient {

    private final WebClient client = createWebClient();

    @Test
    public void streamingResponse() {
        this.client.get()
                .uri("/lec02/product/stream")
                .retrieve()
                .bodyToFlux(Product.class)
                // Por si en vez de los 10 productos que emite este endpoint en 5sg, queremos terminar antes.
                .take(Duration.ofSeconds(3))
                // A partir de aquí ya tenemos un Flux<Product> y podemos trabajar en un pipeline reactivo normal.
                .doOnNext(print())
                // Con then() no se mandará nada al downstream, solo la señal de complete o de error.
                .then()
                // Añadimos StepVerifier no para hacer ningún test, sino para evitar hacer como en Lec01MonoTest,
                // donde bloqueábamos el hilo principal para que no terminara antes que esta operación.
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}
