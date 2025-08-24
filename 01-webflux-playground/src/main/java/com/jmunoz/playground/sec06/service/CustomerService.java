package com.jmunoz.playground.sec06.service;

import com.jmunoz.playground.sec06.dto.CustomerDto;
import com.jmunoz.playground.sec06.mapper.EntityDtoMapper;
import com.jmunoz.playground.sec06.repository.CustomerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Flux<CustomerDto> getAllCustomers() {
        return this.customerRepository.findAll()
                .map(EntityDtoMapper::toDto);
    }

    public Flux<CustomerDto> getAllCustomers(Integer page, Integer size) {
        // pageNumber es 0 indexed, por eso restamos 1.
        return this.customerRepository.findBy(PageRequest.of(page - 1, size))
                .map(EntityDtoMapper::toDto);
    }

    public Mono<CustomerDto> getCustomerById(Integer id) {
        return this.customerRepository.findById(id)
                .map(EntityDtoMapper::toDto);
    }

    // Quien llame a este méto-do debe proveer un Publisher de tipo CustomerDto.
    // Faltarían validaciones de entrada (viene el nombre, viene el email...), pero lo haremos más adelante.
    // Por ahora solo queremos exponer nuestras APIS, asumiendo el camino feliz.
    public Mono<CustomerDto> saveCustomer(Mono<CustomerDto> mono) {
        // Lo que decimos es: mapea de dto a entidad, haz el save y el resultado lo mapeas de entidad a dto.
        // Ese dto es el que se devuelve.
        return mono.map(EntityDtoMapper::toEntity)
                .flatMap(this.customerRepository::save)
                .map(EntityDtoMapper::toDto);
    }

    public Mono<CustomerDto> updateCustomer(Integer id, Mono<CustomerDto> mono) {
        // Lo que decimos es: dado el id obtén el mono<customer>. Si lo encontramos lo siguiente es que
        // nos subscribimos al mono que nos viene como parámetro para obtener el dto, y este dto lo mapeamos
        // a un entity.
        // Como el entity no tendrá el id, lo establecemos usando el parámetro id de entrada.
        // Luego lo grabamos en BD y mapeamos el resultado del save (un entity) a un dto, que es lo que devolvemos.
        return this.customerRepository.findById(id)
                .flatMap(entity -> mono)
                .map(EntityDtoMapper::toEntity)
                .doOnNext(c -> c.setId(id)) // this is safe
                .flatMap(this.customerRepository::save)
                .map(EntityDtoMapper::toDto);
    }

    // Dejamos este méto-do para ver cual es el problema de devolver Mono<Void>
//    public Mono<Void> deleteCustomerById(Integer id) {
//        return this.customerRepository.deleteById(id);
//    }

    public Mono<Boolean> deleteCustomerById(Integer id) {
        return this.customerRepository.deleteCustomerById(id);
    }
}
