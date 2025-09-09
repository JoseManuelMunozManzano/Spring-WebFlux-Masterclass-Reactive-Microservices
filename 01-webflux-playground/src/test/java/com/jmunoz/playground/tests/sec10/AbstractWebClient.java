package com.jmunoz.playground.tests.sec10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Consumer;

// Esta sección NO contiene ningún test.
// Clase abstracta que será extendida por varias clases donde jugaremos con WebClient.
// Aquí tendremos métodos de utilidad.
abstract class AbstractWebClient {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebClient.class);

    protected <T> Consumer<T> print() {
        return item -> log.info("received: {}", item);
    }

    // Configuraciones por defecto.
    // Aceptamos el builder, pero no hacemos nada con el.
    protected WebClient createWebClient() {
        return createWebClient(b -> {});
    }

    // En esta clase tenemos lo común para crear un WebClient.
    // Con el consumer aplicamos al builder lo que nos falte.
    protected WebClient createWebClient(Consumer<WebClient.Builder> consumer) {
        var builder = WebClient.builder()
                .baseUrl("http://localhost:7070/demo03");

        consumer.accept(builder);

        return builder.build();
    }
}
