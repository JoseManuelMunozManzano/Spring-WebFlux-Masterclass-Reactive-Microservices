package com.jmunoz.playground.sec05.exceptions;

import reactor.core.publisher.Mono;

// Actúa como un Exception Factory.
// Hay factory methods para proveer señales de error.
public class ApplicationExceptions {

    public static <T> Mono<T> customerNotFound(Integer id) {
        return Mono.error(new CustomerNotFoundException(id));
    }

    public static <T> Mono<T> missingName() {
        return Mono.error(new InvalidInputException("Name is required"));
    }

    public static <T> Mono<T> missingValidEmail() {
        return Mono.error(new InvalidInputException("Valid email is required"));
    }
}
