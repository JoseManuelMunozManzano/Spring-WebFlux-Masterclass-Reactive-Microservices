package com.jmunoz.aggregator;

import org.junit.jupiter.api.BeforeAll;
import org.mockserver.client.MockServerClient;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// El puerto cambia (es dinámico) y por eso indicamos ${mockServerPort} (ver documentación de MockServer).
@MockServerTest
@AutoConfigureWebTestClient
@SpringBootTest(properties = {
        "customer.service.url=http://localhost:${mockServerPort}",
        "stock.service.url=http://localhost:${mockServerPort}"
})
abstract class AbstractIntegrationTest {

    // Para poder leer el fichero donde tenemos los JSON con los responseBody esperados.
    private static final Path TEST_RESOURCES_PATH = Path.of("src/test/resources");

    // Gracias a la anotación @MockServerTest, automáticamente se inyecta MockServerClient (no hace falta @Autowired)
    protected MockServerClient mockServerClient;

    @Autowired
    protected WebTestClient client;

    @BeforeAll
    public static void setup() {
        // Como sale mucho logging de MockServer, lo vamos a deshabilitar.
        // Pero para debugging es excelente, así que tenerlo en cuenta y
        // comentar esta línea si algo falla, para ver por donde puede ir el problema.
        ConfigurationProperties.disableLogging(true);
    }

    // Para leer los ficheros JSON con los responseBody esperados.
    protected String resourceToString(String relativePath) {
        // Uso readString porque no espero que el fichero sea de 2 GB, será pequeño.
        try {
            return Files.readString(TEST_RESOURCES_PATH.resolve(relativePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
