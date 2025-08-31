package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.Product;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.UUID;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec09/product/{id}`
// El valor de id puede ir de 1 a 100.
// Espera que se envíe un nuevo bearer token `Authorization: Bearer [generate new token]` cada vez.
// Para enviar un nuevo token se usa: UUID.randomUUID().toString().replace("-", "")
// También creamos otro Filter Function para añadir funcionalidad de logging.
public class Lec09ExchangeFilterTest extends AbstractWebClient {

    private static final Logger log = LoggerFactory.getLogger(Lec09ExchangeFilterTest.class);

    // Para construir el WebClient adjuntamos nuestro Exchange Filter, para que WebClient lo invoque.
    private final WebClient client = createWebClient(b -> b.filter(tokenGenerator()).filter(requestLogger()));

    @Test
    public void exchangeFilter() {
        for (int i = 1; i <= 5; i++) {
            this.client.get()
                    .uri("/lec09/product/{id}", i)
                    // Aquí, como parte de nuestra clase de servicio, podemos añadir atributos.
                    // Solo los índices pares tendrán logging.
                    .attribute("enable-logging", i % 2 == 0)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .doOnNext(print())
                    .then()
                    .as(StepVerifier::create)
                    .expectComplete()
                    .verify();
        }
    }

    // Mantenemos la lógica de generación de tokens en otro sitio.
    // Para fines de demostración la dejamos en esta clase, pero debería estar en una clase de utilidad o una de configuración.
    private ExchangeFilterFunction tokenGenerator() {
        return (request, next) -> {
            var token = UUID.randomUUID().toString().replace("-", "");
            log.info("generated token: {}", token);

            // Pasamos el token a nuestra request.
            // Pero cuidado porque la petición no se puede mutar, es inmutable. Esto falla.
            // request.headers().setBearerAuth(token);
            // Tenemos que crear una nueva ClientRequest builder usando la request.
            var modifiedRequest = ClientRequest.from(request).headers(h -> h.setBearerAuth(token)).build();

            // Pasamos al siguiente manejador nuestra request modificada.
            return next.exchange(modifiedRequest);
        };
    }

    // Exchange Filter Function para hacer log del méto-do HTTP y la URL.
    // Añadimos atributos a nuestra funcionalidad de logger.
    // Nuestra clase de servicio va a enviar un flag para habilitar o no este logging.
    private ExchangeFilterFunction requestLogger() {
        return (request, next) -> {
            // Miramos el atributo enable-logging, cuyo valor por defecto es false.
            var isEnabled = (Boolean) request.attributes().getOrDefault("enable-logging", false);
            if (isEnabled) {
                log.info("request url - {}: {}", request.method(), request.url().getPath());
            }

            return next.exchange(request);
        };
    }

}
