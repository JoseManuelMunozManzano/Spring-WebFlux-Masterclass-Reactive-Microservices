package com.jmunoz.playground.sec09.controller;

import com.jmunoz.playground.sec09.dto.ProductDto;
import com.jmunoz.playground.sec09.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ProductDto> saveProduct(@RequestBody Mono<ProductDto> mono) {
        return this.service.saveProduct(mono);
    }

    // Tiene un filtro de precio máximo. Igual o por debajo de ese precio notifica.
    @GetMapping(value = "/stream/{maxPrice}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductDto> productStream(@PathVariable Integer maxPrice) {
        return this.service.productStream()
                .filter(dto -> dto.price() <= maxPrice);
    }

}
