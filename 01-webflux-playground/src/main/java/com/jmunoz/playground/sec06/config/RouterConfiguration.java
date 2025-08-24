package com.jmunoz.playground.sec06.config;

import com.jmunoz.playground.sec06.exceptions.CustomerNotFoundException;
import com.jmunoz.playground.sec06.exceptions.InvalidInputException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

// Para probar RouterConfigurationPractices, comentar este @Configuration.
// En caso contrario, comentar el @Configuration de RouterConfigurationPractices y descomentar este.
@Configuration
public class RouterConfiguration {

    private final CustomerRequestHandler customerRequestHandler;
    private final ApplicationExceptionHandler exceptionHandler;

    public RouterConfiguration(CustomerRequestHandler customerRequestHandler, ApplicationExceptionHandler exceptionHandler) {
        this.customerRequestHandler = customerRequestHandler;
        this.exceptionHandler = exceptionHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> customerRoutes() {
        // Podemos usar esta utility class para crear rápidamente un RequestPredicate.
//        RequestPredicates.

        // En cada verbo (GET...) tenemos que indicar un HandlerFunction con el que obtenemos un objeto ServerRequest y,
        // usándolo, tendremos que devolver un Mono de ServerResponse.
        // Es decir, no vamos directamente a la capa de servicio, sino a un RequestHandler donde extraemos las
        // variables de la ruta... para obtener la información que necesitemos de la petición. Con esta información,
        // ya podemos acceder a la capa de servicio.
        // Es un poco de bajo nivel, pero tenemos mucha flexibilidad.
        //
        // NOTA: Se puede indicar "customers" o "/customers" y ambos funcionan.
        return RouterFunctions.route()
                // Uso de un RequestPredicate. Devolvemos true o false.
                // Es parecido al patrón chain of responsibility, es decir, chequeamos cuál es capaz de manejar la petición.
                // En este ejemplo, cualquier petición GET va a ejecutar allCustomers().
//                .GET(req -> true, this.customerRequestHandler::allCustomers)
                .GET("/customers", this.customerRequestHandler::allCustomers)
                .GET("/customers/paginated", this.customerRequestHandler::paginatedCustomers)
                // Añadimos un RequestPredicate.
                // Indicamos que el path puede ser cualquier cosa, pero le debe seguir un 1 seguido forzosamente de un solo carácter más.
//                .GET("/customers/{id}", RequestPredicates.path("*/1?"), this.customerRequestHandler::getCustomers)
                .GET("/customers/{id}", this.customerRequestHandler::getCustomers)
                .POST("/customers", this.customerRequestHandler::saveCustomers)
                .PUT("/customers/{id}", this.customerRequestHandler::updateCustomers)
                .DELETE("/customers/{id}", this.customerRequestHandler::deleteCustomers)
                // Gestionamos las excepciones.
                // Indicamos el nombre de la excepción y una BiFunction.
                // Vamos a montar un Problem Detail, pero devolvemos Mono<ServerResponse>
                .onError(CustomerNotFoundException.class, this.exceptionHandler::handleException)
                .onError(InvalidInputException.class, this.exceptionHandler::handleException)
                .build();
    }
}
