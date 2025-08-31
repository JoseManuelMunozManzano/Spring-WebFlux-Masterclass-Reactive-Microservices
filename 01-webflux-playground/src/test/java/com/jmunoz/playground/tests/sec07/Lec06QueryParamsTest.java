package com.jmunoz.playground.tests.sec07;


import com.jmunoz.playground.tests.sec07.dto.CalculatorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.Map;

// Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`.
// Vamos a trabajar con el endpoint `/demo02/lec06/calculator`
// Hace la operación matemática y devuelve el resultado.
// Ambos parámetros numéricos deben ser mayores que cero. (¡QUERY PARAMS!)
// El parámetro `operación` tendrá uno de estos valores: `+, -, *, /`.
public class Lec06QueryParamsTest extends AbstractWebClient {

    private final WebClient client = createWebClient();

    @Test
    public void UriBuilderVariables() {
        var path = "/lec06/calculator";
        var query = "first={first}&second={second}&operation={operation}"; // Se elimina ?

        this.client.get()
                .uri(builder -> builder.path(path).query(query).build(10, 20, "+"))
                .retrieve()
                .bodyToMono(CalculatorResponse.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    // También podemos usar un Map
    @Test
    public void UriBuilderMap() {
        var path = "/lec06/calculator";
        var query = "first={first}&second={second}&operation={operation}"; // Se elimina ?
        var map = Map.of(
                "first", 10,
                "second", 20,
                "operation", "*"
        );

        this.client.get()
                .uri(builder -> builder.path(path).query(query).build(map))
                .retrieve()
                .bodyToMono(CalculatorResponse.class)
                .doOnNext(print())
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    // Si los query parameters son opcionales, podemos usar Optional:
    // client.get().uri(b -> b.queryParamIfPresent("key", Optional.empty()).build())
}
