package com.jmunoz.playground.tests.sec10;

import com.jmunoz.playground.tests.sec10.dto.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

// No olvidar ejecutar `java -jar external-services.jar`
public class Lec02Http2Test extends AbstractWebClient {

    private final WebClient client = createWebClient(b -> {
        // Creamos un provider
        // Para HTTP2 solo necesitamos una conexión.
        var poolSize = 1;
        var provider = ConnectionProvider.builder("jomuma")
                .lifo()
                .maxConnections(poolSize)
                .build();

        // Este es el cliente HTTP Reactor.
        // Para HTTP2 tenemos que indicar el protocolo con el que el cliente va a estar "más a gusto".
        // Indicamos H2C (HTTP/2 Cleartext)
        // Usaremos H2 (en vez de H2C) cuando habilitamos SSL/TLS (certificados de seguridad). En caso contrario usaremos H2C.
        var httpClient = HttpClient.create(provider)
                .protocol(HttpProtocol.H2C)
                .compress(true)
                .keepAlive(true);

        // Una vez tenemos el provider y el client, lo pasamos al builder.
        b.clientConnector(new ReactorClientHttpConnector(httpClient));
    });

    @Test
    public void concurrentRequests() {
        // Probar con valores 3, 100 y 10000
        // Ejecutamos: watch 'netstat -an| grep -w 127.0.0.1.7070'
        // En cada caso, vemos que se establece una sola conexión.
        var max = 10_000;
        Flux.range(1, max)
                .flatMap(this::getProduct, max)
                .collectList()
                .as(StepVerifier::create)
                .assertNext(l -> Assertions.assertEquals(max, l.size()))
                .expectComplete()
                .verify();
    }

    private Mono<Product> getProduct(int id) {
        return this.client.get()
                .uri("/product/{id}", id)
                .retrieve()
                .bodyToMono(Product.class);
    }
}
