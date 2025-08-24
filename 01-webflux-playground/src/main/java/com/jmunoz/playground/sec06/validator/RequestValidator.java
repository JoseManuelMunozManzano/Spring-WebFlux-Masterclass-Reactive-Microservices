package com.jmunoz.playground.sec06.validator;

import com.jmunoz.playground.sec06.dto.CustomerDto;
import com.jmunoz.playground.sec06.exceptions.ApplicationExceptions;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

// Tendremos aquí utility methods.
public class RequestValidator {

    // Méto-do que hace la validación.
    public static UnaryOperator<Mono<CustomerDto>> validate() {
        return mono -> mono.filter(hasName())
                .switchIfEmpty(ApplicationExceptions.missingName())
                .filter(hasValidEmail())
                .switchIfEmpty(ApplicationExceptions.missingValidEmail());
    }

    private static Predicate<CustomerDto> hasName() {
        return dto -> Objects.nonNull(dto.name());
    }

    private static Predicate<CustomerDto> hasValidEmail() {
        // Sería mejor añadir un regex indicando las reglas que hacen válido un email.
        return dto -> Objects.nonNull(dto.email()) && dto.email().contains("@");
    }
}
