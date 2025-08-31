package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec07/product/{id}`
// El valor de id puede ir de 1 a 100.
// Si no se envían las credenciales `username: java, password: secret` devuelve 401.
public class Lec07BasicAuthTest extends AbstractWebClient {

    // Las credenciales se indican en el header.
    private final WebClient client = createWebClient(b -> b.defaultHeaders(h -> h.setBasicAuth("java", "secret")));

    @Test
    public void basicAuth() {
        this.client.get()
                .uri("/lec07/product/{id}", 1)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}
