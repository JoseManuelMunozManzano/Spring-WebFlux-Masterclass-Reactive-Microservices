package com.jmunoz.playground.tests.sec02;

import com.jmunoz.playground.sec02.dto.OrderDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

public class Lec04DatabaseClientTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(Lec04DatabaseClientTest.class);

    // Usando DatabaseClient podemos ejecutar cualquier SQL sin necesidad de tener un repository.
    @Autowired
    private DatabaseClient client;

    @Test
    public void orderDetailsByProduct() {
        var query = """
                SELECT co.order_id,
                       c.name AS customer_name,
                       p.description AS product_name,
                       co.amount,
                       co.order_date
                FROM customer c
                INNER JOIN customer_order co ON c.id = co.customer_id
                INNER JOIN product p ON p.id = co.product_id
                WHERE p.description = :description
                ORDER BY co.amount DESC
                """;
        // Usamos el méto-do bind() (hay varias posibilidades que admiten distintos tipos de datos)
        // para indicar los parámetros del SQL. Admite chaining.
        // Para proyectar el resultado se usa map (hay varias posibilidades también).
        // Es decir, bind es para input y map para output.
        // Luego indicamos si queremos recuperar solo un record, el primero, todos...
        // Esto nos devuelve un Flux<OrderDetails>
        //
        // Y luego, lo de siempre.
        this.client.sql(query)
                .bind("description", "iphone 20")
                .mapProperties(OrderDetails.class)
                .all()
                .doOnNext(dto -> log.info("{}", dto))
                .as(StepVerifier::create)
                .assertNext(dto -> Assertions.assertEquals(975, dto.amount()))
                .assertNext(dto -> Assertions.assertEquals(950, dto.amount()))
                .expectComplete()
                .verify();
    }
}
