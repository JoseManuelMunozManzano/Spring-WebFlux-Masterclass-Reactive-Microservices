package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec08/product/{id}`
// El valor de id puede ir de 1 a 100.
// Si no se envía el bearer token `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9` devuelve 401.
public class Lec08BearerAuthTest extends AbstractWebClient {

    // El bearer token se indica en el header.
    private final WebClient client = createWebClient(b -> b.defaultHeaders(h -> h.setBearerAuth("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")));

    @Test
    public void bearerAuth() {
        this.client.get()
                .uri("/lec08/product/{id}", 1)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}
