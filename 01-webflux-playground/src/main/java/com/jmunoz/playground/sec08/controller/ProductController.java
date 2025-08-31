package com.jmunoz.playground.sec08.controller;

import com.jmunoz.playground.sec08.dto.ProductDto;
import com.jmunoz.playground.sec08.dto.UploadResponse;
import com.jmunoz.playground.sec08.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // En otras clases el parámetro era un Mono, porque queríamos crear un solo customer.
    // Pero esta vez esperamos un request body en streaming y por eso usamos Flux.
    @PostMapping(value = "upload", consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<UploadResponse> uploadProducts(@RequestBody Flux<ProductDto> flux) {
        // Los logs los hemos añadido para poder entender ciertas cosas de este ejercicio.
        // Sabremos cuando se invoca este méto-do y veremos en las pruebas que este log,
        // independientemente de la cantidad de productos, solo se ejecuta 1 vez.
        log.info("invoked");

        // Guardamos el producto.
        //    - El doOnNext no haría falta, pero de nuevo indicamos un log para saber qué está ocurriendo.
        //    - Para la prueba de 1 millón de productos lo quitamos.
        // Con then() esperamos que el proceso se complete y obtenemos la cuenta de productos.
        // Devolvemos un objeto UploadResponse.
        //
        // Descomentar cuando la prueba no es la de crear 1 millón de productos.
        //return this.service.saveProducts(flux.doOnNext(dto -> log.info("received: {}", dto)))
        return this.service.saveProducts(flux)
                .then(this.service.getProductsCount())
                .map(count -> new UploadResponse(UUID.randomUUID(), count));
    }

    @GetMapping(value = "download", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ProductDto> downloadProducts() {
        return this.service.allProducts();
    }

}
