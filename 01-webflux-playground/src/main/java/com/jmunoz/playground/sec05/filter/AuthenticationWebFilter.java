package com.jmunoz.playground.sec05.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

@Order(1)
@Service
public class AuthenticationWebFilter implements WebFilter {

    // Solo para probar que funciona ProblemDetail (ir el final del código también)
    // Comentar después de probar.
    //
//    @Autowired
//    private FilterErrorHandler errorHandler;

    // Tenemos dos tipos distintos de tokens, de ahí este Map.
    private static final Map<String, Category> TOKEN_CATEGORY_MAP = Map.of(
            "secret123", Category.STANDARD,
            "secret456", Category.PRIME
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var token = exchange.getRequest().getHeaders().getFirst("auth-token");

        if (Objects.nonNull(token) && TOKEN_CATEGORY_MAP.containsKey(token)) {
            // Pasamos a AuthorizationWebFilter el atributo de la categoría.
            exchange.getAttributes().put("category", TOKEN_CATEGORY_MAP.get(token));
            return chain.filter(exchange);
        }

        // Rechazamos.
        // Emitimos señal empty, que es un Mono<Void> y devolvemos 401.
        //
        // Descomentar para el uso normal.
        return Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));

        // Descomentar si queremos probar ProblemDetail.
//        return errorHandler.sendProblemDetail(exchange, HttpStatus.UNAUTHORIZED, "Set the valid token");
    }
}
