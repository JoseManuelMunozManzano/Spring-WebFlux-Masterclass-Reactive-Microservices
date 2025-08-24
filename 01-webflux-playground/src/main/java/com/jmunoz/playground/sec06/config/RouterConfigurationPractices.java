package com.jmunoz.playground.sec06.config;

import com.jmunoz.playground.sec06.exceptions.CustomerNotFoundException;
import com.jmunoz.playground.sec06.exceptions.InvalidInputException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

// No queremos cargarlo, es solo para aprendizaje.
//@Configuration
public class RouterConfigurationPractices {

    private final CustomerRequestHandler customerRequestHandler;
    private final ApplicationExceptionHandler exceptionHandler;

    public RouterConfigurationPractices(CustomerRequestHandler customerRequestHandler, ApplicationExceptionHandler exceptionHandler) {
        this.customerRequestHandler = customerRequestHandler;
        this.exceptionHandler = exceptionHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> customerRoutes1() {
        // En cada verbo (GET...) tenemos que indicar un HandlerFunction con el que obtenemos un objeto ServerRequest y,
        // usándolo, tendremos que devolver un Mono de ServerResponse.
        // Es decir, no vamos directamente a la capa de servicio, sino a un RequestHandler donde extraemos las
        // variables de la ruta... para obtener la información que necesitemos de la petición. Con esta información,
        // ya podemos acceder a la capa de servicio.
        // Es un poco de bajo nivel, pero tenemos mucha flexibilidad.
        //
        // NOTA: Se puede indicar "customers" o "/customers" y ambos funcionan.
        return RouterFunctions.route()
                // NOTA: Se usa path solo cuando queremos usar Lógica de enrutamiento anidada basada en rutas.
                // Decimos, los path que contengan "customers" búscalos primero en customerRoutes2, y luego
                // continúa por el POST y si hay algún error, lo tratamos al final de este méto-do.
                .path("customers", this::customerRoutes2)
//                .GET("/customers", this.customerRequestHandler::allCustomers)
//                .GET("/customers/paginated", this.customerRequestHandler::paginatedCustomers)
//                .GET("/customers/{id}", this.customerRequestHandler::getCustomers)
                .POST("/customers", this.customerRequestHandler::saveCustomers)
                .PUT("/customers/{id}", this.customerRequestHandler::updateCustomers)
                .DELETE("/customers/{id}", this.customerRequestHandler::deleteCustomers)
                // Gestionamos las excepciones.
                // Indicamos el nombre de la excepción y una BiFunction.
                // Vamos a montar un Problem Detail, pero devolvemos Mono<ServerResponse>
                .onError(CustomerNotFoundException.class, this.exceptionHandler::handleException)
                .onError(InvalidInputException.class, this.exceptionHandler::handleException)
                // Usamos WebFilter. Es lo primero que se ejecuta y se ejecutan en orden de aparición.
                // Este podría ser un WebFilter para autenticación que se ejecuta primero.
                .filter(((request, next) -> {
                    // Para pasar la request al siguiente filter.
                    return next.handle(request);
                    // Para rechazar la petición.
//                    return ServerResponse.badRequest().build();
                }))
                // Este podría ser el WebFilter para autorización que se ejecuta el segundo si pasa el primer WebFilter.
                .filter(((request, next) -> {
                    return next.handle(request);
                }))
                .build();
    }

    // - Varios RouterFunctions.
    // En esta clase del curso vemos que podemos tener varios métodos RouterFunction que se exponen como beans.
    // En el primer méto-do se comentan los endpoints GET y aquí los exponemos.
    // Si se usa esta opción comentar el méto-do path() en customerRoutes1().
    // Es solo para dar una idea.
//    @Bean
//    public RouterFunction<ServerResponse> customerRoutes2() {
//    private RouterFunction<ServerResponse> customerRoutes2() {
//        return RouterFunctions.route()
//                .GET("/customers", this.customerRequestHandler::allCustomers)
//                .GET("/customers/paginated", this.customerRequestHandler::paginatedCustomers)
//                .GET("/customers/{id}", this.customerRequestHandler::getCustomers)
//                .build();
//    }

    // - Lógica de enrutamiento anidada basada en rutas.
    // En esta otra clase del curso vemos que no todos los métodos tienen que ser beans.
    // El RouterFunction de alto nivel es customerRoutes1(),
    // donde se comentan los endpoints GET, y aquí los exponemos y, además, se usa el méto-do path(),
    // con lo que aquí tenemos que quitar la parte del path que se ha indicado en customerRoutes1(), que es "customers"
    // Vemos que aquí no se indica la anotación @Bean y el méto-do es private.
    private RouterFunction<ServerResponse> customerRoutes2() {
        return RouterFunctions.route()
                .GET("/paginated", this.customerRequestHandler::paginatedCustomers)
                .GET("/{id}", this.customerRequestHandler::getCustomers)
                // El orden importa, ¡cuidado porque entran todos por aquí!
                // Nos lo llevamos al final.
                .GET(this.customerRequestHandler::allCustomers)
                .build();
    }
}
