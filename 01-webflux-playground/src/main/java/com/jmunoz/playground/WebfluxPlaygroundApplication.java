package com.jmunoz.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

// ${sec} viene de application.properties.
// R2DBC es diferente a la programación normal y tiene sus propiedades
// separadas, es decir, no usa @SpringBootApplication(scanBasePackages...)
// Es por eso que añadimos la anotación @EnableR2dbcRepositories con la propiedad basePackages.
@SpringBootApplication(scanBasePackages = "com.jmunoz.playground.${sec}")
@EnableR2dbcRepositories(basePackages = "com.jmunoz.playground.${sec}")
public class WebfluxPlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxPlaygroundApplication.class, args);
	}

}
