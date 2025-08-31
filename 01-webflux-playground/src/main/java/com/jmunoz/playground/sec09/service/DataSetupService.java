package com.jmunoz.playground.sec09.service;

import com.jmunoz.playground.sec09.dto.ProductDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

// Cuando la aplicación se arranque, esta clase se ejecutará automáticamente.
// Añadimos un producto cada segundo.
@Service
public class DataSetupService implements CommandLineRunner {

    private final ProductService productService;

    public DataSetupService(ProductService productService) {
        this.productService = productService;
    }

    // Creamos un producto cada segundo.
    @Override
    public void run(String... args) throws Exception {
        Flux.range(1, 1000)
                .delayElements(Duration.ofSeconds(1))
                .map(i -> new ProductDto(null, "product-" + i, ThreadLocalRandom.current().nextInt(1, 100)))
                .flatMap(dto -> this.productService.saveProduct(Mono.just(dto)))
                .subscribe();
    }
}
