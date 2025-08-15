package com.jmunoz.playground.sec02.repository;

import com.jmunoz.playground.sec02.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {

    Flux<Product> findByPriceBetween(int from, int to);

    // Spring recuperará solo los registros basados en el input indicado.
    // Se puede usar el méto-do findBy o findAllBy, son lo mismo.
    Flux<Product> findBy(Pageable pageable);
}
