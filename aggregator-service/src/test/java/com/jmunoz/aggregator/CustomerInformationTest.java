package com.jmunoz.aggregator;

import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

public class CustomerInformationTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CustomerInformationTest.class);

    @Test
    public void customerInformation() {
        // given
        mockCustomerInformation("customer-service/customer-information-200.json", 200);

        // then
        // Si queremos que falle indicar en balance 10001, por ejemplo.
        getCustomerInformation(HttpStatus.OK)
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Sam")
                .jsonPath("$.balance").isEqualTo(10000)
                .jsonPath("$.holdings").isNotEmpty();
    }

    @Test
    public void customerNotFound() {
        // given
        mockCustomerInformation("customer-service/customer-information-404.json", 404);

        // then
        getCustomerInformation(HttpStatus.NOT_FOUND)
                .jsonPath("$.detail").isEqualTo("Customer [id=1] is not found")
                .jsonPath("$.title").isNotEmpty();
    }

    private void mockCustomerInformation(String path, int responseCode) {
        // Este responseBody puede ser muy grande, afectando a la legibilidad del test.
        // Para evitar esto, se crea el directorio /test/resources/customer-service, y dentro el
        // fichero customer-information-200.json
//        var responseBody = """
//                {
//                    "id": -1,
//                    "name": "sam"
//                }
//                """;
        var responseBody = this.resourceToString(path);

        // Lo que decimos es:
        //    Cuando haya una petición a /customers/1
        //    Responde de esta manera
        //        Si no se incluye el statusCode por defecto indica 200, pero es mejor indicarlo explícitamente.
        mockServerClient
                .when(HttpRequest.request("/customers/1"))
                .respond(
                        HttpResponse.response(responseBody)
                                .withStatusCode(responseCode)
                                .withContentType(MediaType.APPLICATION_JSON)
                );
    }

    private WebTestClient.BodyContentSpec getCustomerInformation(HttpStatus expectedStatus) {
        // Indicar /customers/2 si queremos que devuelva un ProblemDetail.
        return this.client.get()
                .uri("/customers/1")
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(e -> log.info("{}", new String(Objects.requireNonNull(e.getResponseBody()))));
    }
}
