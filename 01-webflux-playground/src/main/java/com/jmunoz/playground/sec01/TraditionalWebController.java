package com.jmunoz.playground.sec01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.util.List;

// Llamamos al remoto product service para obtener product response.
// No olvidar ejecutar external-service.jar.
//
// En una aplicación real tendríamos una clase de configuración separada para RestClient donde expondríamos el bean.
//
// Dejar baseUrl en la definición de restClient es muy útil cuando tenemos varios entornos como dev, qa..., para
// evitar tener que actualizar las URLs en todos lados, solo en un sitio.
@RestController
@RequestMapping("traditional")
public class TraditionalWebController {

    private static final Logger log = LoggerFactory.getLogger(TraditionalWebController.class);
    private final RestClient restClient = RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory())
            .baseUrl("http://localhost:7070")
            .build();

    @GetMapping("products")
    public List<Product> getProducts() {
        var list = this.restClient.get()
                .uri("/demo01/products")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {
                });
        log.info("received response: {}", list);
        return list;
    }

    // Error típico. Parece programación reactiva, pero no lo es.
    // Por el hecho de devolver un Flux no significa que sea programación reactiva.
    // Sigue siendo código síncrono bloqueante (la parte restClient) y al final se envía como un Flux.
    // Si además, cancelamos a mitad de la petición, vemos que no lo cancela. Esto es porque
    // para parar (o cualquier otra acción), to-do tiene que ser parte del pipeline reactivo.
    @GetMapping("products2")
    public Flux<Product> getProducts2() {
        var list = this.restClient.get()
                .uri("/demo01/products")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {
                });
        log.info("received response: {}", list);

        return Flux.fromIterable(list);
    }

    // El endpoint /demo01/products/notorious falla.
    @GetMapping("products3")
    public List<Product> getProducts3() {
        var list = this.restClient.get()
                .uri("/demo01/products/notorious")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {
                });
        log.info("received response: {}", list);
        return list;
    }
}
