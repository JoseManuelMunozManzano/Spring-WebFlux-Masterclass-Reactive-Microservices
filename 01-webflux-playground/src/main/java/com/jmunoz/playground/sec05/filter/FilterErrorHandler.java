package com.jmunoz.playground.sec05.filter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// Revisar si desde un WebFilter ya se puede emitir una señal de error o un throw
// de una excepción que pueda capturar nuestro Controller Advice.
//
// En caso contrario, esta es una solución temporal para añadir un ProblemDetail.
// Es feo, pero hace el trabajo.
// SOLO SI REALMENTE LO NECESITAMOS.
@Service
public class FilterErrorHandler {

    @Autowired
    private ServerCodecConfigurer codecConfigurer;
    private ServerResponse.Context context;

    @PostConstruct
    private void init() {
        this.context = new ContextImpl(codecConfigurer);
    }

    // Esta es la lógica que envía ProblemDetail via un ServerWebExchange.
    // Lo que realmente hace feo el código es la necesidad de acceder al context.
    public Mono<Void> sendProblemDetail(ServerWebExchange serverWebExchange, HttpStatus httpStatus, String message) {
        var problem = ProblemDetail.forStatusAndDetail(httpStatus, message);
        return ServerResponse.status(httpStatus)
                .bodyValue(problem)
                .flatMap(sr -> sr.writeTo(serverWebExchange, this.context));
    }

    // Para acceder al context, necesitamos crear este record.
    // Son herramientas de bajo nivel.
    private record ContextImpl(ServerCodecConfigurer codecConfigurer) implements ServerResponse.Context {

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return this.codecConfigurer.getWriters();
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return List.of();
        }
    }
}
