package com.jmunoz.playground.tests.sec10;

import com.jmunoz.playground.tests.sec10.dto.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

import java.time.Duration;

// No olvidar ejecutar `java -jar external-services.jar`
// Probar cada test de forma separada, viendo también su sección del README.md.
public class Lec01HttpConnectionPoolingTest extends AbstractWebClient {

    // Para el test concurrentRequests dejar este.
    // O para el test concurrentRequests2 con el máximo de conexiones permitidas por WebClient por defecto (500).
//    private final WebClient client = createWebClient();

    // Para el test concurrentRequests2 dejar este.
    // Esta es la forma de configurar WebClient para permitir más conexiones que las por defecto (500).
    // SOLO PARA FINES DEMOSTRATIVOS. PROBABLEMENTE, NO VAMOS A NECESITAR ESTO.
    // YA QUE SI LA RESPUESTA TOMA 100ms => 500 / (100 ms) ==> 5000 req / sec.
    private final WebClient client = createWebClient(b -> {
        // Creamos un provider
        // El nombre del builder da igual, he puesto jomuma.
        // Se recomienda usar LIFO en vez de FIFO para que intente cerrar las conexiones más antiguas.
        // pendingAcquireMaxCount -> Si el máximo es 501 peticiones, el resto (si quiero 1000) los ponemos en la cola.
        //      Como si fuera el tamaño de la cola.
        var poolSize = 10000;     // Intercambiar entre 501 y 10000 en función del test
        var provider = ConnectionProvider.builder("jomuma")
                .lifo()
                .maxConnections(poolSize)
                .pendingAcquireMaxCount(poolSize * 5)
                .build();

        // Este es el cliente HTTP Reactor.
        // El problema de personalizar esto es que perdemos keep-alive, gzip, to-do.
        // Así que tenemos que configurarlo también aquí.
        var httpClient = HttpClient.create(provider)
                .compress(true)
                .keepAlive(true);

        // Una vez tenemos el provider y el client, lo pasamos al builder.
        b.clientConnector(new ReactorClientHttpConnector(httpClient));
    });

    // No es realmente un test, es para hacer pruebas de Connection Pooling, de peticiones concurrentes al servicio
    // remoto.
    @Test
    public void concurrentRequests() throws InterruptedException {
        // Cuantas peticiones vamos a enviar a la vez (ver ejemplos con valor 1, 3 y 10)
        // Como flatMap es parallel enviaremos las peticiones a la vez.
        var max = 10;
        Flux.range(1, max)
                .flatMap(this::getProduct)
                .collectList()
                .as(StepVerifier::create)
                .assertNext(l -> Assertions.assertEquals(max, l.size()))
                .expectComplete()
                .verify();

        // Bloqueamos el hilo principal porque el test finaliza tras 5 sg y entonces la conexión se cierra.
        // Así mantenemos viva la conexión.
        // Esto sirve para el comando: watch 'netstat -an| grep -w 127.0.0.1.7070'
        Thread.sleep(Duration.ofMinutes(1));
    }

    // Clase Configuring Connection Pool Size.
    @Test
    public void concurrentRequests2() {
        // Responder 250 peticiones lleva 5 segundos.
        // Responder 260 peticiones lleva 10 segundos.
        // Esta diferencia se debe a que flatMap() se ejecuta en paralelo (parallel) hasta 256,
        // siendo este número el tamaño de su cola.
        //
        // Pero podemos ajustar la concurrencia de flatMap()
        // Si indicamos max, entonces el tamaño de la cola será tan grande como el indicado en la variable,
        // tardando de nuevo 5sg en completar todas las peticiones (260 o 500).
        //
        // ¡¡PERO!!, por defecto WebClient puede manejar hasta 500 conexiones a un servicio remoto (es configurable)
        // Es decir, sin configurar nada, si indicamos max = 501, el responder a esas 501 peticiones
        // tomará un tiempo de 10 segundos.
        // Con la configuración arriba indicada, nos llevará 5 segundos.
        var max = 501;
        Flux.range(1, max)
                .flatMap(this::getProduct, max)
                .collectList()
                .as(StepVerifier::create)
                .assertNext(l -> Assertions.assertEquals(max, l.size()))
                .expectComplete()
                .verify();
    }

    // Clase SocketException - Too Many Open Files Issue
    @Test
    public void concurrentRequests3() {
        var max = 10000;
        Flux.range(1, max)
                .flatMap(this::getProduct, max)
                .collectList()
                .as(StepVerifier::create)
                .assertNext(l -> Assertions.assertEquals(max, l.size()))
                .expectComplete()
                .verify();
    }

    private Mono<Product> getProduct(int id) {
        return this.client.get()
                .uri("/product/{id}", id)
                .retrieve()
                .bodyToMono(Product.class);
    }
}
