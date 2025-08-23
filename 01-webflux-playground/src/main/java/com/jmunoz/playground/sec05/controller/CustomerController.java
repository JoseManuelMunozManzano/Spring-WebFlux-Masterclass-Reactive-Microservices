package com.jmunoz.playground.sec05.controller;

import com.jmunoz.playground.sec05.dto.CustomerDto;
import com.jmunoz.playground.sec05.exceptions.ApplicationExceptions;
import com.jmunoz.playground.sec05.filter.Category;
import com.jmunoz.playground.sec05.service.CustomerService;
import com.jmunoz.playground.sec05.validator.RequestValidator;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

// Emitimos señal de error en vez de ResponseEntity si ocurre algún problema.
@RestController
@RequestMapping("customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Accedemos a los atributos establecidos en los WebFilters usando la anotación @RequestAttribute a nivel
    // de parámetros e indicando el atributo al que queremos acceder. Indicamos el tipo y el nombre del parámetro.
    @GetMapping
    public Flux<CustomerDto> allCustomers(@RequestAttribute("category")Category category) {
        System.out.println(category);
        return this.customerService.getAllCustomers();
    }

    @GetMapping("paginated")
    public Mono<List<CustomerDto>> allCustomers(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "3") Integer size) {
        return this.customerService.getAllCustomers(page, size)
                .collectList();
    }

    // Comparar con la misma clase de sec03
    @GetMapping("{id}")
    public Mono<CustomerDto> getCustomer(@PathVariable Integer id) {
        return this.customerService.getCustomerById(id)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(id));
    }

    // Comparar con la misma clase de sec03
    @PostMapping
    public Mono<CustomerDto> saveCustomer(@RequestBody Mono<CustomerDto> mono) {
        // as() nos devuelve la instancia actual y nos permite mantener el chaining.
        // this es el mono validado.
        // Si validate() emite una señal de error, sabemos que no llega al as(), va
        // directamente al ControllerAdvice.
        return mono.transform(RequestValidator.validate())
                .as(this.customerService::saveCustomer);
    }

    // Comparar con la misma clase de sec03
    @PutMapping("{id}")
    public Mono<CustomerDto> updateCustomer(@PathVariable Integer id, @RequestBody Mono<CustomerDto> mono) {
        // Ejecutamos validate(). Si hay error ahí para.
        // Si no hay error ejecutamos update. Si to-do va bien ahí para.
        // Si hay algún error ejecutamos customerNotFound
        return mono.transform(RequestValidator.validate())
                .as(validReq -> this.customerService.updateCustomer(id, validReq))
                .switchIfEmpty(ApplicationExceptions.customerNotFound(id));
    }

    // Comparar con la misma clase de sec03
    @DeleteMapping("{id}")
    public Mono<Void> deleteCustomer(@PathVariable Integer id) {
        return this.customerService.deleteCustomerById(id)
                .filter(b -> b)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(id))
                .then();    // Como deleteCustomerById devuelve un boolean, usando then() lo cambiamos a Mono<Void>
    }
}
