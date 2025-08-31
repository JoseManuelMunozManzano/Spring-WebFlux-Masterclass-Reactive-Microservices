package com.jmunoz.playground.sec09.config;

import com.jmunoz.playground.sec09.dto.ProductDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class ApplicationConfig {

    // Usamos Many porque vamos a emitir muchos mensajes.
    @Bean
    public Sinks.Many<ProductDto> sink() {
        // Usamos replay para emitir los mensajes emitidos anteriormente para los subscribers que acaben de subscribirse.
        // En este caso, a los subscribers tardíos solo les llega el último mensaje emitido.
        return Sinks.many().replay().limit(1);
    }
}
