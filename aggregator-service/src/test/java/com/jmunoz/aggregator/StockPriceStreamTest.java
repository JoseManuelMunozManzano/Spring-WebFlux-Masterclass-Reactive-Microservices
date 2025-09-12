package com.jmunoz.aggregator;

import com.jmunoz.aggregator.dto.PriceUpdate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.test.StepVerifier;

public class StockPriceStreamTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(StockPriceStreamTest.class);

    @Test
    public void priceStream() {
        // given
        // mock stock-service streaming response.
        var responseBody = this.resourceToString("stock-service/stock-price-stream-200.jsonl");
        mockServerClient
                .when(HttpRequest.request("/stock/price-stream"))
                .respond(
                        HttpResponse.response(responseBody)
                                .withStatusCode(200)
                                // El media type indicado se ve en Swagger.
                                .withContentType(MediaType.parse("application/x-ndjson"))
                );

        // then
        // we should get the streaming response via aggregator-service.
        this.client.get()
                .uri("/stock/price-stream")
                .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().is2xxSuccessful()
                // Es una respuesta en streaming, por lo que no podemos usar expectBody.
                // Con returnResult obtenemos FluxExchangeResult()
                .returnResult(PriceUpdate.class)
                .getResponseBody()
                // Para fines de debug indicamos el log.
                .doOnNext(price -> log.info("{}", price))
                // Para comenzar las aserciones esperadas
                .as(StepVerifier::create)
                .assertNext(p -> Assertions.assertEquals(53, p.price()))
                .assertNext(p -> Assertions.assertEquals(54, p.price()))
                .assertNext(p -> Assertions.assertEquals(55, p.price()))
                .expectComplete()
                // Y lo que lanza to-do.
                .verify();
    }
}
