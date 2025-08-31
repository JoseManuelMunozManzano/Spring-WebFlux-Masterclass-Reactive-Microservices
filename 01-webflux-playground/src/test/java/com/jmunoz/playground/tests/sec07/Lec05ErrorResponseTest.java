package com.jmunoz.playground.tests.sec07;

import com.jmunoz.playground.tests.sec07.dto.CalculatorResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec05/calculator/{first}/{second}`
// Hace la operación matemática y devuelve el resultado.
// Ambos parámetros deben ser mayores que cero.
// En el header mandamos `operación` y alguna de estas operaciones como valor `+, -, *, /`.
// En caso contrario la operación fallará con status 404 y devolverá un objeto ProblemDetail.
public class Lec05ErrorResponseTest extends AbstractWebClient {

    // Para ver el ProblemDetail.
    private static final Logger log = LoggerFactory.getLogger(Lec05ErrorResponseTest.class);
    private final WebClient client = createWebClient();

    @Test
    public void handlingError() {
        this.client.get()
                .uri("/lec05/calculator/{a}/{b}", 10, 20)
                // Para que genere una excepción indicamos el valor @
                // Si queremos que no genere excepciones, indicar +
                .header("operation", "@")
                .retrieve()
                .bodyToMono(CalculatorResponse.class)
                // El ProblemDetail enviado desde el external service puede accederse como parte de la respuesta.
                // Aquí indicamos cualquier tipo de excepción (genérico) y podemos hacer lo que queramos con ella.
                .doOnError(WebClientResponseException.class, e -> log.info("{}", e.getResponseBodyAs(ProblemDetail.class)))
                // Manejando errores en el pipeline reactivo.
                //
                // En este caso, si se emite cualquier señal de error, devolvemos esta respuesta por defecto.
                // .onErrorReturn(new CalculatorResponse(0, 0, null, 0.0))
                //
                // Si queremos tener diferentes respuestas de error dependiendo del error.
                .onErrorReturn(WebClientResponseException.InternalServerError.class, new CalculatorResponse(0, 0, null, 0.0))
                .onErrorReturn(WebClientResponseException.BadRequest.class, new CalculatorResponse(0, 0, null, -1.0))
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    // Usamos el méto-do exchange() en vez de retrieve(), ya que vamos a trabajar con data de bajo nivel,
    // como response headers, cookies...
    @Test
    public void exchange() {
        this.client.get()
                .uri("/lec05/calculator/{a}/{b}", 10, 20)
                // Cambiar de @ a + si queremos un caso exitoso.
                .header("operation", "@")
                // Usando exchange
                .exchangeToMono(this::decode)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    // Ahora tenemos acceso a cookies, headers... y hacemos lo que queramos.
    // Esto es solo un ejemplo.
    private Mono<CalculatorResponse> decode(ClientResponse clientResponse) {
        // clientResponse.cookies()
        // clientResponse.headers()

        log.info("status code: {}", clientResponse.statusCode());

        // En caso de cualquier error, en vez de devolver un CalculatorResponse, vamos a trabajar con una respuesta ProblemDetail
        if (clientResponse.statusCode().isError()) {
            return clientResponse.bodyToMono(ProblemDetail.class)
                    .doOnNext(pd -> log.info("{}", pd))
                    // Para que no falle, ya que espera un Mono<CalculatorResponse> devolvemos una señal empty.
                    .then(Mono.empty());
        }

        // Si to-do va bien
        return clientResponse.bodyToMono(CalculatorResponse.class);
    }
}
