package com.jmunoz.playground.tests.sec08;

import com.jmunoz.playground.sec08.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.time.Duration;

// Solo para hacer la demo, no son tests realmente.
// No olvidar modificar `application.properties` para cambiar la property `sec=sec08` y ejecutar nuestra app `WebfluxPlaygroundApplication`
// Por último, ejecutar este cliente, métodos upload() primero y download() después.
public class ProductsUploadDownloadTest {

    private static final Logger log = LoggerFactory.getLogger(ProductsUploadDownloadTest.class);
    private final ProductClient productClient = new ProductClient();

    @Test
    public void upload() {
        // Es un flux de un solo item que se emitirá pasados 10 sg.
        //
        // var flux = Flux.just(new ProductDto(null, "iphone", 1000))
        //        .delayElements(Duration.ofSeconds(10));
        //
        // Es un flux de 10 items. Cada uno de los items tiene un delay de 2sg antes de emitirse.
        //
        // var flux = Flux.range(1, 10)
        //        .map(i -> new ProductDto(null, "product-" + i, i))
        //       .delayElements(Duration.ofSeconds(2));
        //
        // Prueba con 1 millón de productos, sin delay!!!
        var flux = Flux.range(1, 1_000_000)
                .map(i -> new ProductDto(null, "product-" + i, i));

        // Aunque el flux se emite pasados 10sg (o 2sg, depende de la prueba), uploadProducts() se invoca inmediatamente.
        // Esto es porque no es bloqueante.
        // Esto de esperar 10sg (o 2sg) es para poder ver el funcionamiento interno de todas las piezas.
        this.productClient.uploadProducts(flux)
                .doOnNext(r -> log.info("received: {}", r))
                // Es no bloqueante. Se indica el then() para que no termine el hilo principal.
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    public void download() {
        this.productClient.downloadProducts()
                .map(ProductDto::toString)
                .as(flux -> FileWriter.create(flux, Path.of("products.txt")))
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}
