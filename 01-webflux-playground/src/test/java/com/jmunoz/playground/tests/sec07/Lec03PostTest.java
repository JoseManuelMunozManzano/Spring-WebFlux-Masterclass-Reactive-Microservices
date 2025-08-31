package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

// Vamos a trabajar con el endpoint `/demo02/lec03/product`
// Este endpoint acepta una petición POST para un producto y le lleva 1 segundo responder.
// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`
public class Lec03PostTest extends AbstractWebClient {

    private final WebClient client = createWebClient();

    // En esta clase vemos la diferencia entre usar bodyValue o body.
    // bodyValue - Lo usaremos si tenemos un objeto o un DTO en MEMORIA (esto es lo importante, que esté en memoria)
    // body - Lo usaremos si tenemos un tipo Publisher.
    @Test
    public void postBodyValue() {
        // Usando bodyValue.
        // Tenemos en memoria un objeto.
        var product = new Product(null, "iphone", 1000);

        this.client.post()
                .uri("/lec03/product")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(Product.class)
                // A partir de aquí ya tenemos un Mono<Product> y podemos trabajar en un pipeline reactivo normal.
                .doOnNext(print())
                // Con then() no se mandará nada al downstream, solo la señal de complete o de error.
                .then()
                // Añadimos StepVerifier no para hacer ningún test, sino para evitar hacer como en Lec01MonoTest,
                // donde bloqueábamos el hilo principal para que no terminara antes que esta operación.
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    public void postBody() {
        // Usando body
        // Tenemos un tipo publisher, un mono en este caso, y lo emitimos tras 1sg de retraso.
        var mono = Mono.fromSupplier(() -> new Product(null, "iphone", 1000))
                .delayElement(Duration.ofSeconds(1));

        this.client.post()
                .uri("/lec03/product")
                // De forma asíncrona mandamos el request body (1sg)
                .body(mono, Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                // A partir de aquí ya tenemos un Mono<Product> y podemos trabajar en un pipeline reactivo normal.
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
