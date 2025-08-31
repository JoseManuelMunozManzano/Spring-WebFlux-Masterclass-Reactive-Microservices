package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.Map;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec04/product/{id}`
// Este endpoint espera un header con la propiedad `caller-id`, es decir, estaríamos indicando
// quién llama al servicio. Si no viene este header, no enviaremos la respuesta.
public class Lec04HeaderTest extends AbstractWebClient {

    // Personalizamos nuestro header para esta petición.
    // Si nuestro header personalizado tuviera que enviarse en cada petición, podríamos sobreescribir el header.
    private final WebClient client = createWebClient(b -> b.defaultHeader("caller-id", "order-service"));

    // Personalizando el header para una petición.
    @Test
    public void defaultHeader() {
        this.client.get()
                .uri("/lec04/product/{id}", 1)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    // Sobreescribiendo el header si tenemos que mandarlo en todas las peticiones.
    @Test
    public void overrideHeader() {
        this.client.get()
                .uri("/lec04/product/{id}", 1)
                // El valor indicado al declarar la variable client se sobreescribe aquí.
                .header("caller-id", "new-value")
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    // Si tenemos muchas cabeceras que configurar, podemos usar un map.
    @Test
    public void headersWithMap() {
        var map = Map.of(
                "caller-id", "new-value",
                "some-key", "some-value"
        );

        this.client.get()
                .uri("/lec04/product/{id}", 1)
                // Uso de map
                .headers(httpHeaders -> httpHeaders.setAll(map))
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}
