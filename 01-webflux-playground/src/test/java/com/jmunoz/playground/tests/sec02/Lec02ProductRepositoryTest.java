package com.jmunoz.playground.tests.sec02;

import com.jmunoz.playground.sec02.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.test.StepVerifier;

public class Lec02ProductRepositoryTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(Lec02ProductRepositoryTest.class);

    @Autowired
    private ProductRepository repository;

    @Test
    public void findByPriceBetween() {
        // Usamos el query method finbByPriceBetween(750, 1000) de ProductRepository, que devuelve un Flux<Product>
        // Escribimos cada uno de los items obtenidos.
        // Creamos el StepVerifier.
        // En la tabla Product tenemos 3 registros que cumplen esa condición, así que esos son los que esperamos.
        // Esperamos que el provider emita la señal onComplete()
        // Y con verify() nos subscribimos y ejecutamos el test.
        this.repository.findByPriceBetween(750, 1000)
                .doOnNext(p -> log.info("{}", p))
                .as(StepVerifier::create)
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    public void pageable() {
        // PageRequest es una implementación de la interface Pageable.
        // Indicamos el número de página (basado en índice 0) y su tamaño.
        // En el test se indica la página 0 y 3 de tamaño.
        // Se puede indicar también el tipo de ordenación que se quiere.
        this.repository.findBy(PageRequest.of(0, 3).withSort(Sort.by("price").ascending()))
                .doOnNext(p -> log.info("{}", p))
                .as(StepVerifier::create)
                .assertNext(p -> Assertions.assertEquals(200, p.getPrice()))
                .assertNext(p -> Assertions.assertEquals(250, p.getPrice()))
                .assertNext(p -> Assertions.assertEquals(300, p.getPrice()))
                .expectComplete()
                .verify();
    }
}
