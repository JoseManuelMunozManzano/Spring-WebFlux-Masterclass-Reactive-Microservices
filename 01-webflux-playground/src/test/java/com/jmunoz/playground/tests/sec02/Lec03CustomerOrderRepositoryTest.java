package com.jmunoz.playground.tests.sec02;

import com.jmunoz.playground.sec02.repository.CustomerOrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

public class Lec03CustomerOrderRepositoryTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(Lec03CustomerOrderRepositoryTest.class);

    @Autowired
    private CustomerOrderRepository repository;

    @Test
    public void productsOrderedByCustomer() {
        // Usamos el query method getProductsOrderderByCustomer("mike") de CustomerOrderRepository, que devuelve un Flux<Product>
        // Se prueba el uso de @Query con consulta compleja.
        // Escribimos cada uno de los items obtenidos.
        // Creamos el StepVerifier.
        // En la tabla Product tenemos 2 registros que cumplen esa condición, así que esos son los que esperamos.
        //    No necesitamos probar más, solo si se ejecuta o no el SQL.
        // Esperamos que el provider emita la señal onComplete()
        // Y con verify() nos subscribimos y ejecutamos el test.
        this.repository.getProductsOrderderByCustomer("mike")
                .doOnNext(p -> log.info("{}", p))
                .as(StepVerifier::create)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    // Testing de un ejemplo de projection.
    @Test
    public void orderDetailsByProduct() {
        this.repository.getOrderDetailsByProduct("iphone 20")
                .doOnNext(dto -> log.info("{}", dto))
                .as(StepVerifier::create)
                .assertNext(dto -> Assertions.assertEquals(975, dto.amount()))
                .assertNext(dto -> Assertions.assertEquals(950, dto.amount()))
                .expectComplete()
                .verify();
    }
}
