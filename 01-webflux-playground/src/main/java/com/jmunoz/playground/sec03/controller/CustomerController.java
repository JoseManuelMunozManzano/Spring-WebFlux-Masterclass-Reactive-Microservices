package com.jmunoz.playground.sec03.controller;

import com.jmunoz.playground.sec03.dto.CustomerDto;
import com.jmunoz.playground.sec03.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

// Por ahora no nos preocupamos de los status code. Vamos a asumir que ocurre el camino feliz
// y más adelante abordaremos este tema.

@RestController
@RequestMapping("customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Flux<CustomerDto> allCustomers() {
        return this.customerService.getAllCustomers();
    }

    // Quería poner como valor por defecto un size de 10, pero como solo tenemos 10 customers indico 3.
    // Como solo enviamos una cantidad finita de customers, podemos cambiar Flux por Mono, haciendo
    // el collectList()
    @GetMapping("paginated")
    public Mono<List<CustomerDto>> allCustomers(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "3") Integer size) {
        return this.customerService.getAllCustomers(page, size)
                .collectList();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<CustomerDto>> getCustomer(@PathVariable Integer id) {
        return this.customerService.getCustomerById(id)
                // Si encontramos el customer, devolvemos en un Mono el ResponseEntity ok (200).
                .map(ResponseEntity::ok)
                // Cuando customerId no se encuentra, se emitirá la señal empty y queremos devolver un 4xx.
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Asumimos que nuestro cliente nos envía en el body un publisher de tipo Mono<CustomerDto>
    @PostMapping
    public Mono<CustomerDto> saveCustomer(@RequestBody Mono<CustomerDto> mono) {
        return this.customerService.saveCustomer(mono);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<CustomerDto>> updateCustomer(@PathVariable Integer id, @RequestBody Mono<CustomerDto> mono) {
        return this.customerService.updateCustomer(id, mono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Dejamos este méto-do para dejar constancia del problema de Mono<Void> al eliminar.
//    @DeleteMapping("{id}")
//    public Mono<Void> deleteCustomer(@PathVariable Integer id) {
//        // Se encuentre o no el customer, se emite Mono<Void>, que es una señal empty.
//        // Esto es un problema.
//        return this.customerService.deleteCustomerById(id);
//    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteCustomer(@PathVariable Integer id) {
        // Ahora, usando nuestro query method personalizado que devuelve un boolean,
        // realizamos el filtrado, que devolverá de nuevo b si es true y ejecutará
        // el map para devolver el Mono<Void> y ResponseEntity ok.
        // Si es false ejecuta la parte de defaultIfEmpty() que devuelve un 404.
        return this.customerService.deleteCustomerById(id)
                .filter(b -> b)
                .map(b -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
