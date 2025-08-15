package com.jmunoz.playground.tests.sec02;

import com.jmunoz.playground.sec02.entity.Customer;
import com.jmunoz.playground.sec02.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

public class Lec01CustomerRepositoryTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(Lec01CustomerRepositoryTest.class);

    @Autowired
    private CustomerRepository repository;

    @Test
    public void findAll() {
        // Usamos el query method findAll() de CustomerRepository, que devuelve un Flux<Customer>
        // Escribimos cada uno de los items obtenidos.
        // Creamos el StepVerifier.
        // En la tabla Customer tenemos 10 registros, así que esos son los que esperamos.
        // Esperamos que el provider emita la señal onComplete()
        // Y con verify() nos subscribimos y ejecutamos el test.
        this.repository.findAll()
                .doOnNext(c -> log.info("{}", c))
                .as(StepVerifier::create)
                .expectNextCount(10)
                .expectComplete()
                .verify();
    }

    @Test
    public void findById() {
        // Usamos el query method findById(2) de CustomerRepository, que devuelve un Mono<Customer>
        // Escribimos el item que provee el provider.
        // Creamos el StepVerifier.
        // Validamos el nombre del customer obtenido (mirar data.sql para ver los registros informados)
        // Esperamos que el provider emita la señal onComplete()
        // Y con verify() nos subscribimos y ejecutamos el test.
        this.repository.findById(2)
                .doOnNext(c -> log.info("{}", c))
                .as(StepVerifier::create)
                .assertNext(c -> Assertions.assertEquals("mike", c.getName()))
                .expectComplete()
                .verify();
    }

    @Test
    public void findByName() {
        // Usamos el query method findByName("jake") de CustomerRepository, que devuelve un Flux<Customer>
        // Escribimos los items que provee el provider.
        // Creamos el StepVerifier.
        // Validamos el mail del customer obtenido (mirar data.sql para ver los registros informados)
        // Esperamos que el provider emita la señal onComplete()
        // Y con verify() nos subscribimos y ejecutamos el test.
        this.repository.findByName("jake")
                .doOnNext(c -> log.info("{}", c))
                .as(StepVerifier::create)
                .assertNext(c -> Assertions.assertEquals("jake@gmail.com", c.getEmail()))
                .expectComplete()
                .verify();
    }

    @Test
    public void findByEmailEndingWith() {
        // Usamos el query method findByEmailEndingWith("ke@gmail.com") de CustomerRepository, que devuelve un Flux<Customer>
        // Escribimos los items que provee el provider.
        // Creamos el StepVerifier.
        // Validamos el email de los customers obtenidos (mirar data.sql para ver los registros informados)
        // Esperamos que el provider emita la señal onComplete()
        // Y con verify() nos subscribimos y ejecutamos el test.
        this.repository.findByEmailEndingWith("ke@gmail.com")
                .doOnNext(c -> log.info("{}", c))
                .as(StepVerifier::create)
                .assertNext(c -> Assertions.assertEquals("mike@gmail.com", c.getEmail()))
                .assertNext(c -> Assertions.assertEquals("jake@gmail.com", c.getEmail()))
                .expectComplete()
                .verify();
    }

    // Normalmente, suele hacerse tanto insert como delete como parte de un test.
    // Con esto, conseguimos atomizar esta parte y que no afecte a otros tests.
    // Si se hacen por separado, si no ponemos un orden, puede hacerse primero el test de insert y luego el de findAll,
    // con lo que los resultados ya estarían mal.
    @Test
    public void insertAndDeleteCustomer() {
        // insert
        var customer = new Customer();
        customer.setName("marshal");
        customer.setEmail("marshal@gmail.com");
        this.repository.save(customer)
                .doOnNext(c -> log.info("{}", c))
                .as(StepVerifier::create)
                .assertNext(c -> Assertions.assertNotNull(c.getId()))
                .expectComplete()
                .verify();

        // count
        this.repository.count()
                .as(StepVerifier::create)
                .expectNext(11L)
                .expectComplete()
                .verify();

        // delete
        // Una vez elimine, se ejecuta el count().
        this.repository.deleteById(11)
                .then(this.repository.count())
                .as(StepVerifier::create)
                .expectNext(10L)
                .expectComplete()
                .verify();
    }

    @Test
    public void updateCustomer() {
        // Vamos a cambiar el customer ethan por noel.
        // Este cambio lo grabamos usando el operador flatMap() para aplanar el Flux<Mono<Customer>> que nos daría un map()
        // Lo demás ya lo sabemos porque es lo mismo que en los otros tests: imprimir el item tras grabarlo, crear el StepVerifier...
        this.repository.findByName("ethan")
                .doOnNext(c -> c.setName("noel"))
                .flatMap(c -> this.repository.save(c))
                .doOnNext(c -> log.info("{}", c))
                .as(StepVerifier::create)
                .assertNext(c -> Assertions.assertEquals("noel", c.getName()))
                .expectComplete()
                .verify();

    }
}
