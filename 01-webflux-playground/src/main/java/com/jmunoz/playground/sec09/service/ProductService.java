package com.jmunoz.playground.sec09.service;

import com.jmunoz.playground.sec09.dto.ProductDto;
import com.jmunoz.playground.sec09.mapper.EntityDtoMapper;
import com.jmunoz.playground.sec09.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final Sinks.Many<ProductDto> sink;

    public ProductService(ProductRepository repository, Sinks.Many<ProductDto> sink) {
        this.repository = repository;
        this.sink = sink;
    }

    public Mono<ProductDto> saveProduct(Mono<ProductDto> mono) {
        return mono.map(EntityDtoMapper::toEntity)
                .flatMap(this.repository::save)
                .map(EntityDtoMapper::toDto)
                // Una vez el producto se ha guardado y lo mapeamos a un ProductDto,
                // vamos a emitirlo via sink.
                .doOnNext(this.sink::tryEmitNext);
    }

    public Flux<ProductDto> productStream() {
        // El mismo sink actúa como un Flux y podemos devolverlo.
        return this.sink.asFlux();
    }
}
