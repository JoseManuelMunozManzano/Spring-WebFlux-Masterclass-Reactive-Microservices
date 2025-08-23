package com.jmunoz.playground.sec05.filter;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Order(2)
@Service
public class AuthorizationWebFilter implements WebFilter {

    // Al obtener la petición, tenemos que saber como llamar a uno de los méto-dos, prime() o standard().
    // Lo sabemos gracias al atributo `categoria` que nos pasa AuthenticationWebFilter. Por si acaso, por defecto
    // el valor será STANDARD.
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var category = exchange.getAttributeOrDefault("category", Category.STANDARD);
        return switch (category) {
            case STANDARD -> standard(exchange, chain);
            case PRIME -> prime(exchange, chain);
        };
    }

    // Para usuarios PRIME, que pueden hacer lo que quieran, directamente pasamos
    // al controller.
    private Mono<Void> prime(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange);
    }

    // Los usuarios STANDARD solo pueden hacer peticiones GET.
    private Mono<Void> standard(ServerWebExchange exchange, WebFilterChain chain) {
        var isGet = HttpMethod.GET.equals(exchange.getRequest().getMethod());
        if (isGet) {
            return chain.filter(exchange);
        }

        // Rechazamos.
        // Emitimos señal empty, que es un Mono<Void> y devolvemos 403.
        return Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
    }
}
