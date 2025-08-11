package com.jmunoz.playground.sec01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

// Llamamos al remoto product service para obtener product response.
// No olvidar ejecutar external-service.jar.
//
// En vez de usar RestClient como en TraditionalWebController, en programación reactiva se usa WebClient.
// En vez de que el controller devuelva una lista como en TraditionalWebController, en programación reactiva devuelve un Flux<Product>.
@RestController
@RequestMapping("reactive")
public class ReactiveWebController {

    private static final Logger log = LoggerFactory.getLogger(ReactiveWebController.class);
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:7070")
            .build();

    @GetMapping("/products")
    public Flux<Product> getProducts() {
        return this.webClient.get()
                .uri("/demo01/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(p -> log.info("received: {}", p));
    }

    // Indicando este MediaType conseguimos que en el navegador vaya apareciendo
    // cada producto conforme lo obtenemos, de uno en uno.
    @GetMapping(value = "/products/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> getProductsStream() {
        return this.webClient.get()
                .uri("/demo01/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(p -> log.info("received: {}", p));
    }

    // Este endpoint demo01/products/notorious falla tras 4 sg.
    // Vemos que añadiendo onErrorComplete() podemos gestionar el error
    // para cambiar una señal de error a una de complete.
    @GetMapping("/products3")
    public Flux<Product> getProducts3() {
        return this.webClient.get()
                .uri("/demo01/products/notorious")
                .retrieve()
                .bodyToFlux(Product.class)
                // Comentar para ver el error.
                .onErrorComplete()
                .doOnNext(p -> log.info("received: {}", p));
    }
}
