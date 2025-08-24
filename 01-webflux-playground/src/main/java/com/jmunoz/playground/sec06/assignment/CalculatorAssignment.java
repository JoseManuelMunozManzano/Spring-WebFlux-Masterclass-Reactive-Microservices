package com.jmunoz.playground.sec06.assignment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/*
    /calculator/{a}/{b}

    header: operation: +,-,*,/
*/
@Configuration
public class CalculatorAssignment {

    @Bean
    public RouterFunction<ServerResponse> calculator() {
        return RouterFunctions.route()
                .path("calculator", this::calculatorRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> calculatorRoutes() {
        // Este lo hice yo
//        RequestPredicate noOperation = RequestPredicates.headers(headers -> headers.header("operation").isEmpty());
//        RequestPredicate addOperation = RequestPredicates.headers(headers -> headers.header("operation").getFirst().equals("+"));
//        RequestPredicate subtractOperation = RequestPredicates.headers(headers -> headers.header("operation").getFirst().equals("-"));
//        RequestPredicate multiplyOperation = RequestPredicates.headers(headers -> headers.header("operation").getFirst().equals("*"));
//        RequestPredicate divideOperation = RequestPredicates.headers(headers -> headers.header("operation").getFirst().equals("/"));
//        RequestPredicate bZero = RequestPredicates.path("calculator/*/0");
//        RequestPredicate anotherOperation = RequestPredicates.headers(headers -> !headers.header("operation").getFirst().matches("[+\\-*/]"));
//
//
//        return RouterFunctions.route()
//                .GET("/{a}/{b}", noOperation.or(bZero).or(anotherOperation), request -> ServerResponse.badRequest().build())
//                .GET("/{a}/{b}", addOperation, this::add)
//                .GET("/{a}/{b}", subtractOperation, this::subtract)
//                .GET("/{a}/{b}", multiplyOperation, this::multiply)
//                .GET("/{a}/{b}", divideOperation, this::divide)
//                .build();

        // Esto lo hizo el profesor.
        return RouterFunctions.route()
                .GET("/{a}/0", badRequest("b cannot be 0"))
                .GET("/{a}/{b}", isOperation("+"), handle((a, b) -> a + b))
                .GET("/{a}/{b}", isOperation("-"), handle((a, b) -> a - b))
                .GET("/{a}/{b}", isOperation("*"), handle((a, b) -> a * b))
                .GET("/{a}/{b}", isOperation("/"), handle((a, b) -> a / b))
                .GET("/{a}/{b}", badRequest("operation header should be + - * /"))
                .build();
    }

    private RequestPredicate isOperation(String operation) {
        return RequestPredicates.headers(h -> operation.equals(h.firstHeader("operation")));
    }

    private HandlerFunction<ServerResponse> handle(BiFunction<Integer, Integer, Integer> function) {
        return request -> {
            var a = Integer.parseInt(request.pathVariable("a"));
            var b = Integer.parseInt(request.pathVariable("b"));
            var result = function.apply(a, b);
            return ServerResponse.ok().bodyValue(result);
        };
    }

    private HandlerFunction<ServerResponse> badRequest(String message) {
        return request -> ServerResponse.badRequest().bodyValue(message);
    }

    // Esto lo hice yo
//    private Mono<ServerResponse> add(ServerRequest request) {
//        var a = Integer.parseInt(request.pathVariable("a"));
//        var b = Integer.parseInt(request.pathVariable("b"));
//        return ServerResponse.ok().bodyValue(a + b);
//    }
//
//    private Mono<ServerResponse> subtract(ServerRequest request) {
//        var a = Integer.parseInt(request.pathVariable("a"));
//        var b = Integer.parseInt(request.pathVariable("b"));
//        return ServerResponse.ok().bodyValue(a - b);
//    }
//
//    private Mono<ServerResponse> multiply(ServerRequest request) {
//        var a = Integer.parseInt(request.pathVariable("a"));
//        var b = Integer.parseInt(request.pathVariable("b"));
//        return ServerResponse.ok().bodyValue(a * b);
//    }
//
//    private Mono<ServerResponse> divide(ServerRequest request) {
//        var a = Integer.parseInt(request.pathVariable("a"));
//        var b = Integer.parseInt(request.pathVariable("b"));
//        double c = (double) a / b;
//        return ServerResponse.ok().bodyValue(c);
//    }
}
