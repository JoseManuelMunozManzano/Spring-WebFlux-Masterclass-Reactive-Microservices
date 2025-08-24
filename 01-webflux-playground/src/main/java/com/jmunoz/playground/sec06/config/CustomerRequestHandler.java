package com.jmunoz.playground.sec06.config;

import com.jmunoz.playground.sec06.exceptions.ApplicationExceptions;
import com.jmunoz.playground.sec06.dto.CustomerDto;
import com.jmunoz.playground.sec06.service.CustomerService;
import com.jmunoz.playground.sec06.validator.RequestValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

// Como hemos indicado en CustomerRequestHandler, accedemos aquí para extraer la información que necesitamos
// de la petición (path variables, query parameters, request body...) antes de poder acceder a la capa de servicio.
@Service
public class CustomerRequestHandler {

    private final CustomerService customerService;

    public CustomerRequestHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    // GET
    // Para allCustomers no tenemos que extraer ningún tipo de información de la petición.
    public Mono<ServerResponse> allCustomers(ServerRequest request) {
        // Ejemplos para extraer información de la petición.
        // request.pathVariable()
        // request.headers()
        // request.queryParam()

        // Como devolver el Mono<ServerResponse>
        //
        // Indicamos el status que esperamos devolver.
        //
        // Luego indicamos el valor del body.
        //    bodyValue: Usamos el objeto dto directamente, pero lo tenemos que tener en memoria.
        // Pero no podemos usar bodyValue porque no tenemos el objeto en memoria. En WebFlux, al interaccionar
        // con la BBDD, cuando se devuelve la información de getAllCustomers() no se hace como un objeto,
        // sino como un tipo Publisher.
        // En estos casos tenemos que usar body, e indicamos el tipo de item que el publisher emitirá.
        //
        // Luego indicamos contentType(MediaType contentType), pero no hace falta porque Spring WebFlux lo configura
        // automáticamente como JSON. Solo indicar si queremos indicar nuestro propio MediaType.
        return this.customerService.getAllCustomers()
                .as(flux -> ServerResponse.ok().body(flux, CustomerDto.class));
    }

    // GET con query params para paginación.
    public Mono<ServerResponse> paginatedCustomers(ServerRequest request) {
        var page = request.queryParam("page").map(Integer::parseInt).orElse(1);
        var size = request.queryParam("size").map(Integer::parseInt).orElse(3);

        // Utilizamos collectList() porque es una cantidad finita y pequeña de datos.
        return this.customerService.getAllCustomers(page, size)
                .collectList()
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    // GET {id}
    // Necesitamos recuperar el id del path variable.
    // Siempre se obtiene como un String, así que tenemos que transformarlo al tipo de dato que necesitemos.
    //
    // En getCustomersById(), del publisher obtenemos un solo valor. En este caso, podemos usar bodyValue.
    // Usamos flatMap porque bodyValue ya devuelve un Mono<ServerResponse>.
    // Cuando el valor se emite es cuando indicamos ServerResponse.ok() con el valor.
    // Pero si el valor no se emite, tenemos que indicar una señal de error, la excepción que recogerá RouterConfiguration.
    public Mono<ServerResponse> getCustomers(ServerRequest request) {
        var id = Integer.parseInt(request.pathVariable("id"));
        return this.customerService.getCustomerById(id)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(id))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    // POST
    // Necesitamos recuperar el request body.
    // Luego tenemos que hacer las validaciones a ese body, para lo que usamos la función transform()
    // Tras validar, ya tengo el mono validado y lo guardo usando saveCustomer().
    // Tras guardar, ya tengo Mono<CustomerDto> que transformo a un Mono<ServerResponse>
    public Mono<ServerResponse> saveCustomers(ServerRequest request) {
        return request.bodyToMono(CustomerDto.class)
                .transform(RequestValidator.validate())
                .as(this.customerService::saveCustomer)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    // PUT
    // Necesitamos recuperar el id del path variable.
    // Necesitamos recuperar el request body.
    // Luego tenemos que hacer las validaciones a ese body, para lo que usamos la función transform()
    // Tras validar, ya tengo el mono validado y lo actualizo usando updateCustomer().
    // Tras actualizar, ya tengo Mono<CustomerDto> que transformo a un Mono<ServerResponse>
    //
    // Como puede que no obtenga customerDto (señal empty) de updateCustomer(), tenemos que lanzar una excepción.
    public Mono<ServerResponse> updateCustomers(ServerRequest request) {
        var id = Integer.parseInt(request.pathVariable("id"));

        return request.bodyToMono(CustomerDto.class)
                .transform(RequestValidator.validate())
                // En este caso usamos flatMap() en vez de as() porque con as() si paso dos errores:
                // invalidInput del nombre porque no lo mando y un CustomerNotFound como puede ser 1234,
                // nos devuelve el error CustomerNotFound en vez de InvalidInput.
                // Esto es porque as() actúa como un chaining y la validación del request ocurre en la capa de servicio.
                // Pero usando flatMap no hay chaining y si se produce un error (InvalidInput) se sale.
                .flatMap(dto -> this.customerService.updateCustomer(id, Mono.just(dto)))
                .switchIfEmpty(ApplicationExceptions.customerNotFound(id))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    // DELETE
    // Necesitamos recuperar el id del path variable.
    // En deleteCustomerById(), del publisher obtenemos un Mono<boolean> y solo tenemos que continuar con el
    // pipeline si es un true, de ahí el filter.
    // Si es false vamos por el switchIfEmpty y la excepción customerNotFound la recogerá RouterConfiguration.
    // Si es true, lo que tenemos en el pipeline es un Mono<Boolean> y tenemos que construir un Mono<ServerResponse>
    // Para ello usamos el operador then() que devuelve un Mono<Void> si no le pasamos nada, pero que devuelve
    // un Mono<V> si le pasamos un Mono<V>.
    public Mono<ServerResponse> deleteCustomers(ServerRequest request) {
        var id = Integer.parseInt(request.pathVariable("id"));
        return this.customerService.deleteCustomerById(id)
                .filter(b -> b)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(id))
                .then(ServerResponse.ok().build());
    }
}
