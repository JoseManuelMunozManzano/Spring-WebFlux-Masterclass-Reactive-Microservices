package com.jmunoz.playground.sec06.config;

import com.jmunoz.playground.sec06.exceptions.CustomerNotFoundException;
import com.jmunoz.playground.sec06.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Consumer;

// En caso de cualquier señal de error, se dispara este méto-do, basado en la clase de excepción.
// Construimos ProblemDetail y devolvemos un Mono<ServerResponse>
@Service
public class ApplicationExceptionHandler {

    public Mono<ServerResponse> handleException(CustomerNotFoundException ex, ServerRequest request) {
        var status = HttpStatus.NOT_FOUND;
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setType(URI.create("http://example.com/problems/customer-not-found"));
        problem.setTitle("Customer Not Found");
        // A diferencia de la sección 4, paquete advice, donde no hacía falta indicar el path en setInstance porque
        // Spring la generaba sola, aquí, usando endpoints funcionales, si tenemos que indicarlo nosotros.
        problem.setInstance(URI.create(request.path()));
        return ServerResponse.status(status).bodyValue(problem);
    }

    // Usando un helper (por si hay muchas excepciones que tratar)
    public Mono<ServerResponse> handleException(InvalidInputException ex, ServerRequest request) {
        return handleException(HttpStatus.BAD_REQUEST, ex, request, problem -> {
            problem.setType(URI.create("http://example.com/problems/invalid-input"));
            problem.setTitle("Invalid Input");
        });
    }

    // Si hubiera muchos métodos de excepciones, usar un helper como este ayuda bastante a no repetir código.
    private Mono<ServerResponse> handleException(HttpStatus status, Exception ex, ServerRequest request, Consumer<ProblemDetail> consumer) {
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setInstance(URI.create(request.path()));
        consumer.accept(problem);
        return ServerResponse.status(status).bodyValue(problem);
    }
}
