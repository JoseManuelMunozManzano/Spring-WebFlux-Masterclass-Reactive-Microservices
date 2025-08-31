package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

// Simple GET
// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint /demo02/lec01/product/{id}
public class Lec01MonoTest extends AbstractWebClient {

    private final WebClient client = createWebClient();

    @Test
    public void simpleGet() throws InterruptedException {
        this.client.get()
                .uri("/lec01/product/1")
                .retrieve()
                .bodyToMono(Product.class)
                // A partir de aquí ya tenemos un Mono<Product> y podemos trabajar en un pipeline reactivo normal.
                .doOnNext(print())
                // No olvidar que en la programación reactiva se enviará la petición solo cuando alguien se subscriba.
                .subscribe();

        // Si no bloqueamos el hilo principal, el programa termina antes de haberse terminado de ejecutar la petición.
        Thread.sleep(Duration.ofSeconds(2));
    }

    // En este ejemplo vemos como hacer peticiones concurrentes y no bloqueantes.
    // Si cada petición dura 1 sg y hacemos 50 peticiones, to-do el proceso debería durar 50sg, pero
    // si ejecutamos, veremos que obtenemos todos los productos a la vez, ¡en menos de 1sg!
    // Eso sí, se pierde el orden porque distintos threads se encargan de traer distintos productos (thread safe!) concurrentemente.
    @Test
    public void concurrentRequest() throws InterruptedException {
        for (int i = 1; i <= 50 ; i++) {
            this.client.get()
                    .uri("/lec01/product/{id}", i)
                    .retrieve()
                    .bodyToMono(Product.class)
                    // A partir de aquí ya tenemos un Mono<Product> y podemos trabajar en un pipeline reactivo normal.
                    .doOnNext(print())
                    // No olvidar que en la programación reactiva se enviará la petición solo cuando alguien se subscriba.
                    .subscribe();
        }

        // Si no bloqueamos el hilo principal, el programa termina antes de haberse terminado de ejecutar la petición.
        Thread.sleep(Duration.ofSeconds(2));
    }
}
