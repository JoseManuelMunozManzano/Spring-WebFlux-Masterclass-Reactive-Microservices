package com.jmunoz.playground.tests.sec06;

import com.jmunoz.playground.sec06.dto.CustomerDto;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

// NOTA: Son los mismos tests que ejecutamos en sec04 porque tienen que funcionar exactamente igual, ya
// que solo hemos cambiado la forma en la que exponemos los endpoints, usando Functional Endpoints en vez
// de controllers con anotaciones. La funcionalidad no cambia.

// En la anotación indicamos, cuando se ejecuten los tests, que Spring tenga en cuenta la sec06.
// Indicando @AutoConfigureWebTestClient, podemos luego hacer el @Autowired de WebTestClient.
//
// Se valida el detalle de validación de la respuesta.
@AutoConfigureWebTestClient
@SpringBootTest(properties = "sec=sec06")
public class CustomerServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceTest.class);

    @Autowired
    private WebTestClient client;

    // Notar que en uri no indicamos el puerto ni localhost...
    // Esto es gracias a @AutoConfigureWebTestClient, ya que le pedimos a Spring que lo maneje por nosotros.
    // Esto es porque como parte de los tests de integración, el server se ejecutará en un puerto aleatorio.
    // Por tanto, Spring arrancará automáticamente el server y añadirá el base URL por nosotros.
    @Test
    public void allCustomers() {
        this.client.get()
                .uri("/customers")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CustomerDto.class)
                .value(list -> log.info("{}", list))
                .hasSize(10);
    }

    // En este ejemplo se usa JsonPath.
    // Usamos consumeWith para escribir en un log el resultado (viene como bytes)
    @Test
    public void paginatedCustomers() {
        this.client.get()
                .uri("/customers/paginated?page=3&size=2")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(5)
                .jsonPath("$[1].id").isEqualTo(6);
    }

    @Test
    public void customerById() {
        this.client.get()
                .uri("/customers/1")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("sam")
                .jsonPath("$.email").isEqualTo("sam@gmail.com");
    }

    // Ya hemos hablado de por qué se hace a la vez la creación / borrado.
    @Test
    public void createAndDeleteCustomer() {
        // create
        var dto = new CustomerDto(null, "marshal", "marshal@gmail.com");
        this.client.post()
                .uri("/customers")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.id").isEqualTo(11) // Si no sabemos cual es, usar .isNumber()
                .jsonPath("$.name").isEqualTo("marshal")
                .jsonPath("$.email").isEqualTo("marshal@gmail.com");

        // delete
        this.client.delete()
                .uri("/customers/11")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().isEmpty();    // No esperamos un body al hacer un delete.
    }

    @Test
    public void updateCustomer() {
        var dto = new CustomerDto(null, "noel", "noel@gmail.com");
        this.client.put()
                .uri("/customers/10")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.name").isEqualTo("noel")
                .jsonPath("$.email").isEqualTo("noel@gmail.com");
    }

    @Test
    public void customerNotFound() {
        // get
        this.client.get()
                .uri("/customers/11")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.detail").isEqualTo("Customer [id=11] is not found");


        // delete
        this.client.delete()
                .uri("/customers/11")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.detail").isEqualTo("Customer [id=11] is not found");

        // put
        var dto = new CustomerDto(null, "noel", "noel@gmail.com");
        this.client.put()
                .uri("/customers/11")
                .bodyValue(dto)
                .exchange()
                .expectStatus().is4xxClientError()  // o, .isEqualTo(404) para un valor concreto
                .expectBody()
                .consumeWith(r -> log.info("{}", new String(Objects.requireNonNull(r.getResponseBody()))))
                .jsonPath("$.detail").isEqualTo("Customer [id=11] is not found");
    }

    @Test
    public void invalidInput() {
        // missing name
        var missingName = new CustomerDto(null, null, "noel@gmail.com");
        this.client.post()
                .uri("/customers")
                .bodyValue(missingName)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Name is required") ;

        // missing email
        var missingEmail = new CustomerDto(null, "noel", null);
        this.client.post()
                .uri("/customers")
                .bodyValue(missingEmail)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Valid email is required") ;

        // invalid email
        var invalidEmail = new CustomerDto(null, "noel", "noel");
        this.client.put()
                .uri("/customers/10")
                .bodyValue(invalidEmail)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Valid email is required") ;
    }
}
