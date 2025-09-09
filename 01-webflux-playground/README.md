# WebFlux Playground

En esta sección (y para las siguientes) nuestro objetivo es configurar un proyecto de prácticas para aprender `Spring WebFlux`.

Aprenderemos varias características de `WebFlux`, `R2DBC` (Reactive Data Access) y escribiremos tests de integración.

Al final, configuraremos otro proyecto para desarrollar un sistema reactivo.

# Traditional vs Reactive APis

## Project Setup

Para crear el proyecto accederemos a `https://start.spring.io/` e indicaremos estos datos:

![alt Project Setup](./images/01-ProjectSetup.png)

## Traditional vs Reactive API

Vamos a ver como una web reactiva es diferente de una web tradicional.

![alt Traditional Vs Reactive](./images/02-TraditionalVsReactive.png)

Vamos a desarrollar una aplicación muy sencilla, donde asumimos que hay un servicio externo de productos.

Como parte de una petición de entrada a nuestra app, haremos una llamada a este `product service` y, sea lo que sea que este servicio provea, devolveremos esa data a nuestro llamador.

Primero desarrollaremos la aplicación en un estilo de programación tradicional y luego haremos lo mismo en un estilo reactivo, para comprender algunas cosas importantes.

`product service` ya está desarrollado, y para ejecutarlo, solo tenemos que acceder a una terminal y ejecutar: `java -jar external-service.jar`.

Luego, acceder en el navegador al Swagger de este servicio: `http://localhost:7070/webjars/swagger-ui/index.html`.

![alt Swagger](./images/03-ExternalServiceSwagger.png)

Trabajaremos en el endpoint `/demo01/products` donde, al invocarlo, tardará 10sg en obtener 10 productos, 1 por segundo.

Tratamos de simular un servicio remoto lento.

En `src/java/com/jmunoz/playground/sec01` creamos la clase:

- `Product`
  - Es un record.
- `TraditionalWebController`
  - Es un controller hecho en programación tradicional.
- `ReactiveWebController`
  - Es un controller hecho en programación reactiva.

**Testing**

- No olvidar ejecutar `external-services.jar`.

- Ejecutamos `WebFluxPlaygroundApplication`.
  - Traditional en un terminal con `curl`.
    - Usamos `curl` en una terminal para enviar una petición al endpoint hecho en programación tradicional: `curl -N http://localhost:8080/traditional/products`.
      - Vemos en los logs de ejecución de la app que nos lleva 10sg imprimir toda la información, es decir, los 10 productos.
      - En la terminal solo se ven los 10 productos cuando pasan los 10sg.
    - Volvemos a hacer la misma petición `curl`, pero ahora cancelamos la petición (Ctrl+C) tras unos segundos, porque el usuario no quiere esperar 10 sg.
      - Vemos en los logs de ejecución de la app que ha recuperado la lista de productos, aunque la habíamos cancelado.
      - En la terminal no se ve nada.
      - Vemos que en la terminal donde se ejecuta `external-services.jar` sigue esperando 10sg y completa la petición, es decir, aunque hemos cancelado la petición, un controlador tradicional sigue haciendo el trabajo.
  - Reactive en un terminal con `curl`.
    - Usamos `curl` en una terminal para enviar una petición al endpoint hecho en programación reactiva: `curl -N http://localhost:8080/reactive/products`.
      - Usamos la opción `curl -N` porque `curl` usa un buffering y hasta que no obtiene todo, no lo imprime en consola. Usando la opción `-N` no se usa este buffer y podremos ver en consola como se obtienen los productos de uno en uno. 
      - Vemos en los logs de ejecución de la app que cada segundo se recupera un producto.
    - Volvemos a hacer la misma petición `curl`, pero ahora cancelamos la petición (Ctrl+C) tras unos segundos, porque el usuario no quiere esperar 10 sg.
      - Vemos en los logs de ejecución de la app que cada segundo recuperó un producto y, cuando cancelamos, el controller paró inmediatamente.
      - En la terminal ya sabemos que va imprimiéndose cada producto cada segundo.
      - Vemos que en la terminal donde se ejecuta `external-services.jar` también se ha cancelado el trabajo, es decir, para el proceso.

- Ejecutamos `WebFluxPlaygroundApplication`.
  - Reactive en un navegador.
    - Accedemos al navegador usando la ruta `http://localhost:8080/reactive/products`.
      - En el navegador se usa un buffer, por lo que obtenemos de una vez todos los productos pasados 10sg.
      - En los logs de nuestra app obtenemos cada segundo un producto.
    - Accedemos al navegador usando la ruta `http://localhost:8080/reactive/products` y pulsando el botón `refresh` de vez en cuando.
      - Cada vez que pulsamos el botón `refresh`, si vemos los logs de `external-services.jar` veremos que se cancela la petición y vuelve a recibirse una nueva petición. 
  - Traditional en un navegador.
    - Accedemos al navegador usando la ruta `http://localhost:8080/traditional/products`.
      - En el navegador obtenemos los productos cuando pasan 10sg. Esto es independiente del buffering, porque obtenemos una lista.
      - En los logs de nuestra app tenemos que esperar los 10sg para obtener la lista.
    - Accedemos al navegador usando la ruta `http://localhost:8080/traditional/products` y pulsando el botón `refresh` de vez en cuando.
        - Cada vez que pulsamos el botón `refresh`, si vemos los logs de `external-services.jar` veremos que piensa que es una nueva petición, no sabe que se cancela la actual, se van acumulando las peticiones y tenemos que esperar 10sg a que se vayan completando. Hace mucho más trabajo.

## Exposing Streaming API

¿Cómo podemos enviar información del producto en forma de streaming al navegador?

Es decir, ejecutando el endpoint reactivo desde el terminal, conseguíamos ver cada producto conforme lo íbamos obteniendo, pero el navegador esto parece no entenderlo.

En realidad, es fácil.

En `src/java/com/jmunoz/playground/sec01` modificamos la clase:

- `ReactiveWebController`
  - Creamos un endpoint `products/stream` para conseguir ver en el navegador los productos de uno en uno.
  - Lo único que se añade es el `MediaType` `@GetMapping(value = "/products/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)`

Por defecto, `MediaType` es `application/json`. Con esto, el navegador hace el buffering.

Pero si indicamos `MediaType.TEXT_EVENT_STREAM_VALUE` el navegador comprenderá que la respuesta viene en streaming.

**Testing**

- No olvidar ejecutar `external-services.jar`.

- Ejecutamos `WebFluxPlaygroundApplication`.
    - Reactive en un navegador.
        - Accedemos al navegador usando la ruta `http://localhost:8080/reactive/products/stream`.
            - Veremos aparecer en el navegador los productos de uno en uno, conforme llegan.

Más adelante, en otra sección, hablaremos más sobre esto.

## Common Mistake Using Reactive Pipeline

Este es un error típico en el que los desarrolladores creen que están haciendo programación reactiva, cuando no lo es.

En `src/java/com/jmunoz/playground/sec01` modificamos la clase:

- `TraditionalWebController`
  - Creamos un endpoint `products2` que devuelve un `Flux`, pero que no es programación reactiva, sino código síncrono bloqueante.

**Testing**

- No olvidar ejecutar `external-services.jar`.

- Ejecutamos `WebFluxPlaygroundApplication`.
  - Traditional en un terminal con `curl`.
    - Usamos `curl` en una terminal para enviar una petición al endpoint hecho en programación tradicional: `curl -N http://localhost:8080/traditional/products2`.
      - Vemos que obtenemos los 10 productos solo cuando pasan 10sg, no de uno en uno, tanto en la terminal como en los logs de nuestra app. 
      - Aunque cancelemos antes de que pasen 10sg, el servicio externo sigue haciendo el trabajo y en los logs de nuestra app, pasados esos 10sg, seguimos viendo la respuesta con los 10 productos.

## How Reactive Web Works - Step By Step

Vamos a ver como funcionan las webs reactivas.

**Reactive**

Ya sabemos que la programación reactiva está basada en el patrón Publisher/Subscriber.

Al igual que visualizamos todo como objetos en la POO, en la programación reactiva o cuando desarrollamos microservicios reactivos, tenemos que visualizar todo como un publisher o un subscriber.

Por ejemplo, si tenemos un backend y un navegador, el backend actúa como publisher y el navegador como un subscriber.

Una de las preguntas típicas es, ¿cómo sabe mi navegador (o Postman) como subscribirse a un publisher?

Lo que ocurre es que, cuando envían una petición, se establece una conexión. Esto es subscribirse a un publisher.

Para comprender mejor esto, mirar esta imagen:

![alt Publisher/Subscriber](./images/04-Publisher-Subscriber.png)

Es un ejemplo de una app hecha con Spring WebFlux. Estamos exponiendo nuestras APIs via un controller. Este controller devuelve un publisher tipo `Flux<Product>`, y la data la obtenemos de un servicio remoto `product-service` vía uso de `WebClient`.

Este `WebClient` es sencillamente un envoltorio alrededor de `Reactor Netty` (ver `https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming/tree/main`).

Este `WebClient` puede enviar la petición y recibir la respuesta de manera no bloqueante.

Igualmente, si tenemos que hacer llamadas a BD, usaremos `R2DBC` (lo veremos en la próxima sección).

Cuando exponemos las APIs desde el controller, no las exponemos directamente para el navegador, sencillamente devolvemos un tipo publisher como Mono o Flux.

Hay una capa exterior, que es Spring Framework, no la olvidemos.

Cuando el navegador envia una petición a nuestra aplicación, no va directamente al controller, sino que hay un `three way handshake` entre el navegador y Spring para establecer una conexión TCP.

Una vez se ha hecho el `handshake` el navegador manda realmente la petición de los productos y Spring acepta la petición y enruta la petición al controller.

Entonces, Spring verá que el tipo de retorno es un tipo publisher, así que se subscribirá a este publisher.

Así que el navegador se está subscribiendo al Flux, pero no directamente, sino que es el Framework de Spring es el que subscribirá al Flux.

Ya sabemos que hasta que alguien no se subscribe, el publisher por sí solo no ejecuta nada, en el controller solo se construye el pipeline reactivo. Es decir, no enviamos la petición HTTP en este momento.

La petición solo se envía cuando Spring se subscribe a este Flux, y solo entonces `WebClient` mandará la petición a `product-service`.

`WebClient` recibirá la data de manera no bloqueante.

![alt Publisher/Subscriber 2](./images/05-Publisher-Subscriber-2.png)

La primera parte del código la invoca Spring y la segunda parte es un pseudocódigo que muestra como se subscribe y obtenemos el producto.

Una vez lo obtenemos, Spring tomará la información del producto y la escribirá vía la conexión (flush) en el navegador.

![alt Publisher/Subscriber 3](./images/06-Publisher-Subscriber-3.png)

Igualmente, cuando el cliente cancela la petición o cierra el navegador, podemos detectarlo.

En la imagen donde vemos el pseudocódigo, se ve un callback `channel.onClose()`, que se invocará cuando la conexión o el canal se cierre.

Cuando nos subscribimos a `getProducts()` obtenemos el objeto `subscription`, por tanto, en `channel.onClose()` indicamos que cancelamos nuestra subscripción.

Esa subscripción viene del `WebClient`, es decir, si cancelamos, estamos diciendo al `WebClient` que pare de hacer el trabajo, así que el `WebClient` cerrará su canal con `product-service` y este lo detecta y para de hacer el trabajo.

Indicar que ChatGPT trabaja más o menos de forma similar, con streams.

**Traditional**

![alt Publisher/Subscriber 4](./images/07-Publisher-Subscriber-4.png)

Cuando en la manera tradicional de programar devolvemos una lista, Spring invocará el método `getProducts()`.

Como es una lista, es un código de estilo síncrono y bloqueante, así que tenemos que esperar a que `product-service` devuelva a `RestClient` la lista completa.

Solo entonces (cuando pasan los 10sg) podemos escribirla en el canal (flush) y dársela al navegador.

## Should Entire Stack Be Reactive?

Otra pregunta muy común es, cuando usamos reactive, ¿debería ser toda la pila reactiva?

Sería genial si toda la pila fuera reactiva porque obtendríamos todo el potencial del streaming, con `backpressure`...

Sin embargo, cuando tenemos varias aplicaciones como parte de una arquitectura, no es posible migrarlo todo a Spring WebFlux desde el primer día.

Imaginemos que dadas dos aplicaciones, una está migrada y la otra no. No ocurre nada malo con esto.

Está claro que la primera app usará el sistema de recursos más eficientemente, y la segunda app seguirá funcionando como antes.

Cuando tengamos tiempo, migraremos la segunda app a Spring WebFlux para obtener todo su potencial.

## Reactive Web is Resilient - Demo

Volvemos al Swagger de nuestro recurso externo `external-services.jar`.

Trabajaremos en el endpoint `/demo01/products/notorious`. Su funcionamiento es como el anterior, es decir, tratará de darnos 10 productos, donde cada uno tardará un sg. Sin embargo, este servicio fallará mientras procesa la petición.

Lo que intentamos conseguir es, cuando recibimos una petición, usando nuestro `WebClient` enviaremos una petición a `external-services`, que fallará, y veremos qué ocurre.

En `src/java/com/jmunoz/playground/sec01` modificamos la clase:

- `TraditionalWebController`
  - Creamos un endpoint `products3` para llamar a este endpoint y ver que ocurre cuando falla.
- `ReactiveWebController`
  - Creamos un endpoint `products3` para llamar a este endpoint y ver que ocurre cuando falla.

**Testing**

- No olvidar ejecutar `external-services.jar`.

- Ejecutamos `WebFluxPlaygroundApplication`.
  - Traditional en un terminal con `curl`.
    - Usamos `curl` en una terminal para enviar una petición al endpoint hecho en programación tradicional: `curl -N http://localhost:8080/traditional/products3`.
      - Vemos en la terminal de nuestra app que falla la JVM al pasar 4sg y obtenemos un error 500.
      - El servicio externo falla y termina el programa.
  - Reactive en un terminal con `curl`.
    - Usamos `curl` en una terminal para enviar una petición al endpoint hecho en programación tradicional: `curl -N http://localhost:8080/reactive/products3`.
      - Vemos en la terminal de nuestra app que obtenemos 4 productos (respuesta parcial) y luego falla la JVM y obtenemos un error con código 18. 
      - El servicio externo falla y termina el programa.
  
Indicar que la respuesta parcial no puede deserializarse porque comienza con un corchete abierto, pero luego no acaba en un corchete cerrado.

Entonces, ¿para qué? El punto aquí es que es muy fácil de manejar el error usando `onErrorComplete()` para cambiar una señal de error a una de complete.

Con esto, vemos que obtenemos los 4 productos, pero ya no hay error en nuestra app y se cierra el corchete, por lo que ya podemos deserializar el código obtenido, aunque sea parcial. Indicar que `external-services.java` si falla y el programa se termina, pero el nuestro no.

Es decir, nuestra aplicación es más resiliente a errores gracias a operadores como `onErrorComplete()`. Pero si queremos que falle, también podemos hacerlo y lo veremos más adelante.

Sin embargo, en un controller web tradicional no se puede hacer esto. U obtenemos toda la lista o nada, no hay opción de obtener una respuesta con una lista parcial.

## Summary

- Reactive vs Traditional API
- Responsive
  - Reacting quickly
  - Streaming response
  - Cancel

# Spring Data R2DBC

`R2DBC` significa `Reactive Relational Database Connectivity`.

Documentación: 

- `https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/`.
- `https://r2dbc.io/drivers/`.
- `https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/#datatypes`.
- `https://docs.spring.io/spring-data/relational/reference/r2dbc.html`.
- `https://docs.spring.io/spring-data/relational/reference/r2dbc/query-methods.html`.
- `https://medium.com/@padiahrohit/enable-h2-console-in-java-reactive-environmant-dcfcfdd6858a`

## Introduction

Qué es `R2DBC`.

- JPA es una especificación.
  - Se usa en programación tradicional síncrona.
- R2DBC es una especificación separada.
  - Se usa en programación reactiva.
- R2DBC no es exactamente lo mismo que JPA.

Características de `R2DBC`:

- Prioriza
  - Rendimiento
  - Escalabilidad
  - Streaming + Backpressure (se hará una demo con un driver Postgres)
- No tiene características tipo Hibernate como
  - @OneToMany
  - @ManyToMany
  - ...

R2DBC soporta mapeo de objetos de entidad simple.

Para ver documentación de los drivers que soporta actualmente R2DBC ver: `https://r2dbc.io/drivers/`.

En este curso usaremos la BD H2 por motivos de aprendizaje y se hará una demo con un driver Postgres más tarde.

Tenemos `Spring Data R2DBC`, que es un envoltorio alrededor de `R2DBC`.

Aunque ya se ha dicho que no es lo mismo `R2DBC` que `JPA`, si se ha usado `Spring Data JPA`, al usar este nuevo `Spring Data R2DBC` no notaremos ninguna diferencia. Su uso simplifica todo mucho. 

Crearemos una interface y proveeremos `Query Methods` como estos.

![alt Spring Data R2DBC Methods](./images/08-SpringDataR2DBCMethods.png)

Spring hará toda la magia y automáticamente creará los SQL`s. Lo importante es que indicaremos el tipo de retorno publisher (Mono o Flux).

## Connection String

Estas con cadenas de conexión para las BD H2, Postgres y MySql:

![alt R2DBC Drivers](./images/09-R2DBC-Drivers.png)

Indicamos esta tabla como documentación, pero no hace falta indicarlo porque Spring lo hace automáticamente por nosotros.

Estas son las propiedades de configuración que tenemos que indicar en el fichero `application.properties` para poder conectarnos a Postgres.

![alt R2DBC Config](./images/10-R2DBC-Config.png)

**Recursos para R2DBC**

Scripts de Inicialización de BD

- For db initialization scripts
  - `spring.sql.init.data-locations=classpath:sql/data.sql`
- To show SQL
  - `logging.level.org.springframework.r2dbc=TRACE`

## Project Setup

Estas son las tablas (y sus relaciones) del proyecto que se va a realizar en esta sección:

![alt R2DBC Tables](./images/11-R2DBC-Tables.png)

En `src/main/resources/sql` esta el código `data.sql` con los SQL`s necesarios para que Spring pueda crear estas tablas.

Nuestro proyecto lo vamos a hacer en `src/java/com/jmunoz/playground/sec02` pero vamos a tener un problema. 

Cuando Spring se ejecute, va a intentar escanear todos los paquetes y crear los beans. Esto puede causar algún conflicto, como que exista la misma clase de ProductRepository, por ejemplo, en distintos paquetes.

Para evitar esto, vamos a usar la propiedad `scanBasePackages` en `WebfluxPlaygroundApplication.java` para que Spring no escanee todos los paquetes, solo los que le digamos.

Por tanto, creamos/modificamos las clases siguientes:

- `WebfluxPlaygroundApplication.java`
  - El main, donde indicaremos que paquete tiene que escanear Spring para crear sus beans.
- `application.properties`
  - Indicamos la configuración de la BD para R2DBC.

## Customer Entity / Repository

Creamos la clase entity y la interfaz repository para la tabla `customer`.

En `src/java/com/jmunoz/playground/sec02` creamos las clases:

- `Customer`
  - Es la clase entity que representa a la tabla `customer`.
  - En el package entity.
- `CustomerRepository`
  - Es la interfaz repository que usaremos para tener ya creado un CRUD.
  - Extiende de `ReactiveCrudRepository`.
  - En el package repository.

En `src/test/java/com/jmunoz/playground.tests.sec02` creamos la clase:
- `AbstractTest`
  - Es una clase abstracta que extenderemos para hacer tests.

## StepVerifier - Crash Course

Vamos a jugar con R2DBC query methods y a escribir tests durante las siguientes clases.

Es un prerequisito tener nociones de StepVerifier y estos métodos para poder escribir tests:

- StepVerifier.create(...)
- Next
  - expectNext(...)
  - expectNextCount()
  - thenConsumeWhile(...)
  - assertNext(...)
- Complete/Error
  - expectComplete()
  - expectError()
- Verify (¡No olvidarlo! Es quien se subscribe y ejecuta el test)
  - verity()

## CRUD Using Repository

En `src/test/java/com/jmunoz/playground.tests.sec02` creamos la clase:

- `Lec01CustomerRepositoryTest`
  - Es un test sobre la interface CustomerRepository.

Sobre la mutación de objetos (test sobre update) tener en cuenta:

- La programación reactiva es una programación de estilo funcional particularmente para aplicaciones con IO intensivas.
- La programación funcional prefiere funciones puras (sin efectos secundarios)
  - Preferir funciones puras donde sea posible, pero no ciegamente en todas partes.
- ¡Nuestra tabla de BD es mutable! ¡Nuestro objeto entidad es mutable!
  - ¡Mutar está bien!

Para mutar un registro, tenemos que hacerlo via el pipeline reactivo, y usaremos el operador `doOnNext(...)`.

Ejemplo: 

```java
// IO no bloqueante.
// Con doOnNext() podemos mutar objetos.
// Los operadores, para un item, no se invocan concurrentemente. ¡Es secuencial!
this.repository.findById(1)
        .doOnNext(c -> c.setName("sam"));

// Esto es equivalente al estilo tradicional
var customer = getCustomer(1);
customer.setName("sam");
```

## R2DBC - Show SQL

Para poder ver las sentencias SQL que se están ejecutando (siempre para propósitos de debug) tenemos que indicar una propiedad.

Se puede hacer en `application.properties` pero en este caso lo vamos a hacer en la clase `AbstractTest` (aunque la dejamos comentada).

Esta propiedad es realmente a nivel de logger, y es la siguiente:

`logging.level.org.springframework.r2dbc=DEBUG`

## Product Entity / Repository

Creamos la clase entity y la interfaz repository para la tabla `product`.

En `src/java/com/jmunoz/playground/sec02` creamos las clases:

- `Product`
    - Es la clase entity que representa a la tabla `product`.
    - En el package entity.
- `ProductRepository`
    - Es la interfaz repository que usaremos para tener ya creado un CRUD.
    - Extiende de `ReactiveCrudRepository`.
    - En el package repository.

En `src/test/java/com/jmunoz/playground.tests.sec02` creamos la clase:

- `Lec02ProductRepositoryTest`
    - Es un test sobre la interface ProductRepository.

## Pageable

Spring Data R2DBC soporta resultados paginados.

Se usa la interface `Pageable` para hacer peticiones de chunks de data cuando tenemos set de datos muy grandes. Por ejemplo:

- Page 1, Size 10
- Ordenar por Price ascendente

Añadimos la paginación en `ProductRepository` y le hacemos tests en `Lec02ProductRepositoryTest`.

## Complex Queries / Join

Para consultas complejas o joins, es preferible usar SQL, ya que es eficiente y evitamos el problema N+1 (referencias circulares).

Además, R2DBC no tiene anotaciones para `@ManyToMany`... ya que intenta ser muy sencillo y centrarse en rendimiento y escalabilidad.

Es por ello que R2DBC prefiere sentencias SQL para consultas complejas, porque son más eficientes.

Las sentencias SQL pueden ejecutarse de dos formas distintas:

- Usar el Repository.
  - Usamos la anotación `@Query` y la sentencia SQL.
- Database client.

Vamos a ver un ejemplo de cada posibilidad.

## Join Query Using @Query

En `src/java/com/jmunoz/playground/sec02` creamos las clases:

- `CustomerOrder`
    - Es la clase entity que representa a la tabla `customer_order`.
    - En el package entity.
- `CustomerOrderRepository`
    - Es la interfaz repository que usaremos para tener ya creado un CRUD.
    - Extiende de `ReactiveCrudRepository`.
    - En el package repository.

En `src/test/java/com/jmunoz/playground.tests.sec02` creamos la clase:

- `Lec03CustomerOrderRepositoryTest`
    - Es un test sobre la interface CustomerOrderRepository.

## Projection

Vamos a obtener distintos campos de las distintas tablas de las que consta nuestro proyecto. A esto se le llama `projection`.

Podemos crear una clave Java (records) que represente la fila que queremos obtener.

En `src/java/com/jmunoz/playground/sec02` creamos la clase:

- `OrderDetails`
    - Record con la representación de la fila que queremos obtener en la query. 
    - En el package dto.

Añadimos un nuevo método a `CustomerOrderRepository` y lo probamos en `Lec03CustomerOrderRepositoryTest`.

## R2DBC Database Client

Usando DatabaseClient podemos ejecutar cualquier SQL sin necesidad de tener un repository.

En `src/test/java/com/jmunoz/playground.tests.sec02` creamos la clase:

- `Lec04DatabaseClientTest`
    - Es un test para probar SQLs complejos usando DatabaseClient.

## Testing

Para probar los tests, solo tenemos que ir a cada una de las clases de test y ejecutarlos.

# Reactive CRUD APIs with WebFlux

## Introduction

En esta sección vemos como exponer REST APIs con Spring WebFlux, en concreto los relacionados a Customer.

![alt Reactive Controller](./images/04-ReactiveController.png)

En secciones anteriores ya se crearon las clases de Entity y de Repository, así que podemos usarlas y, en esta sección, nos enfocamos en las clases service y controller.

Es decir, solo nos centraremos en los CRUD APIs, no vamos a gestionar excepciones ni validaciones de entrada. Se verán en secciones posteriores. 

En `src/java/com/jmunoz/playground/sec03` creamos los paquetes y clases:

- `controller`
- `dto`
- `entity`
  - `Customer`: Copiado de `sec02/entity`.
- `mapper`
- `repository`
  - `CustomerRepository`: Copiado de `sec02/repository`.
- `service`

## Do we need DTO (Data Transfer Object)?

¿Por qué crear clases DTO que son iguales que las clases entity? ¿Las necesitamos?

La respuesta corta es:

- ¡Depende!
  - No las uses si no te gustan.
  - Todo depende del equipo y de la aplicación.

Una respuesta más elaborada es esta:

- Una clase entity representa la data almacenada en una tabla de BD.
- Una clase DTO representa la API / data que compartimos con los clientes.

Muchas veces parecerán iguales, pero no tiene por qué. Por ejemplo, una clase entity puede tener un campo de password y la clase DTO no debería tener ese campo por razones de seguridad.

O en una clase entity podemos almacenar una fecha en formato UTC, para guardarla en BD, mientras que en una clase DTO vamos a querer devolver al cliente una fecha en formato LocalDateTime.

Una clase DTO también nos permite versionar nuestra API. Por ejemplo, para la misma clase entity, podemos tener la clase `CustomerDto` y más adelante crear `CustomerDtoV2` en la que varía el nombre de un campo, y ambas versiones de DTO siguen funcionando, una para unos clientes y la V2 para otros.

- Ventajas de usar DTO
  - Desacoplamiento (decoupling)
    - Tabla BD vs API
  - Seguridad
    - Exposición de data
    - La entidad Customer podría tener campos como password que la clase DTO no debe tener.
  - Versionado
  - Reglas de validación al hacer POST de data en la API

## DTO / Entity / Repository

En esta clase nos centramos en las clases DTO y Mapper.

En `src/java/com/jmunoz/playground/sec03`, en los paquetes indicados creamos las clases:

- `dto`
  - `CustomerDto`: Es un record.
- `mapper`
  - `EntityDtoMapper`: Es sobre todo, una clase utility para convertir de entity a dto y al revés.

## Service Class Implementation

En esta clase trabajamos en la capa de servicio.

En `src/java/com/jmunoz/playground/sec03`, en los paquetes indicados creamos las clases:

- `service`
  - `CustomerService`

## Controller

En esta clase exponemos las APIs CRUD via un controller.

En `src/java/com/jmunoz/playground/sec03`, en los paquetes indicados creamos la clase:

- `controller`
  - `CustomerController`

## FAQ - @RequestBody Mono<T> vs T

En esta clase vemos la diferencia que hay entre que un cliente nos envíe un body publisher Mono<T> versus que nos envíe un tipo T directamente.

![alt CustomerDto vs Mono<CustomerDto>](./images/12-CustomerDtoVsMonoCustomerDto.png)

Ambos enfoques funcionarán bien, por tanto, ¿hay alguna diferencia?

Si, y para entenderlo, veamos este ejemplo: Imaginemos que tenemos dos microservicios, uno que llama al otro, el Request Body no viene como en la imagen de arriba, porque va a haber un handshake de tres vías entre los dos servicios para establecer la conexión, y luego el cliente transferirá el Request Body como un array de bytes.

Cuando tenemos un DTO como en la imagen de arriba, estamos preguntando por un objeto deserializado y Spring Framework recogerá todos los bytes decodificados al objeto, y luego invocará al método saveCustomer().

En la imagen de abajo, en cambio, tenemos un publisher así que el método puede ser invocado incluso antes de recibir todos los requests. No hay ningún problema, porque realizamos toda la lógica de negocio como parte de los operadores y estos serán ejecutados solo cuando obtengamos la data, como parte del pipeline reactivo.

Por tanto, como parte de la invocación del método, solo construimos el pipeline reactivo.

Entonces, para casos como el de la imagen, no va a haber mucho beneficio y cualquiera de los dos enfoques va a funcionar perfectamente. Pero más tarde hablaremos del streaming y de las ventajas de usar un tipo publisher.

## CRUD APIs Demo

Vamos a probar nuestras APIs usando Postman.

No vamos a tener en cuenta, por ahora, casos en los que no encontremos un customerId, ya que no devolvemos un status 400 Bad Request. Esto lo haremos después.

Modificamos `application.properties` para cambiar la property `sec=sec03` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec03`.

## Mono/Flux - Response Entity

Si en nuestras pruebas hacemos una petición a getCustomer indicando el id 11, que no existe, obtenemos un status code 200, lo que es erroneo, ya que ese id no existe.

En esta clase vamos a devolver HTTP Status Codes.

- Mono/Flux son tipos Publisher
  - data/empty => 200
  - error => 500
- ResponseEntity
  - Mono<ResponseEntity<T>>
    - 400
    - 404
    - 429
    - ...

Como parte de nuestro controller, devolvemos un tipo de publisher, Mono o Flux.

Ya hemos comentado que Spring Framework, por detrás, se subscribirá a nuestros publisher, sea lo que sea que devuelven.

Cuando se subscriban, buscarán la data o la señal empty, y en ambos casos se asumirá que el status code es 200, success.

Pero si se emite una señal de error, se asume que algo malo ha pasado y se devuelve un status code 500, Internal Server Error.

Este es el comportamiento por defecto.

Por eso, cuando preguntamos por un id inexistente, el status code que nos devuelve es 200, porque el publisher ha enviado la señal empty en la response body.

Pero nosotros queremos que nuestra app envíe status code apropiados, como 404, 429..., y para ello tenemos que enviar `Mono<ResponseEntity<T>>` con el status code apropiado, para que Spring Framework devuelva los status code al cliente.

Tener en cuenta una cosa, mientras que `Mono<ResponseEntity<T>>` tiene sentido, `Flux<ResponseEntity<T>>` no lo tiene, porque Flux es streaming, es decir, cuando el cliente envía una petición, el servidor puede enviar una respuesta streaming al cliente, múltiples mensajes al cliente.

![alt Flux => Streaming](./images/13-FluxStreaming.png)

Por tanto, si indicamos `Flux<ResponseEntity<T>>` lo que esto significa NO es que estaremos enviando cada vez 200-Ok, 200-Ok, 200-Ok y 200-Ok, sino que enviaremos una vez 200-Ok y ya, el siguiente no puede ser 400 y el siguiente 500. No podemos cambiarlos.

Es decir, ResponseEntity o status code solo puede establecerse una vez. Cuando el cliente envía una petición, el servidor devuelve ResponseEntity solo una vez al cliente.

Por eso, no tiene sentido un `Flux<ResponseEntity<T>>`, solo `Mono<ResponseEntity<T>>`.

Si realmente necesitamos enviar un Flux, podemos enviar `ResponseEntity<Flux<T>>`. De esta forrma, devolvemos ResponseStatus y los headers inmediatamente de forma bloqueante y síncrona, y el body llegará más tarde, de forma asíncrona. Sin embargo, se sugiere mantener Flux para streaming, porque es ahí donde tiene sentido.

Con `Mono<ResponseEntity<T>>` devolvemos todo, ResponseStatus, headers y body, de forma asíncrona en un momento posterior.

También es posible devolver `Mono<ResponseEntity<Mono<T>>>` o `Mono<ResponseEntity<Flux<T>>>` pero es confuso y es mejor evitarlo.

Ver documentación: `https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-methods/responseentity.html`

## Handling 4XX via Response Entity

Vamos a modificar algunas clases para devolver status code 4XX cuando customerId no existe.

En `src/java/com/jmunoz/playground/sec03`, en los paquetes indicados modificamos las clases:

- `controller`
  - `CustomerController`: Se modifican los métodos `getCustomer(id)` y `updateCustomer(...)` y se identifica un problema en `deleteCustomer(id)`, porque se encuentre o no el customer, se emite Mono<Void>, que es una señal empty, así que, si seguimos los caminos normales, siempre vamos a devolver un 4XX.

El problema indicado al eliminar un customer se arregla en la siguiente clase.

## @Modifying Query

Vamos a corregir el problema del método `deleteCustomer()` añadiendo un query method.

Spring soporta la anotación query `@Modifying`, que sirve para poder pedirle a Spring que devuelva el número de filas afectadas o un booleano (true si hay filas afectadas y false en caso contrario).

Esto hace que, en vez de devolver void, podamos devolver un boolean o un integer, sabiendo así si se ha eliminado un customer.

En `src/java/com/jmunoz/playground/sec03`, en los paquetes indicados modificamos las clases:

- `repository`
  - `CustomerRepository`: Creamos un query method personalizado con la anotación `@Modifying` que devuelva un booleano true si se ha realizado el borrado de un customer.
- `service`
  - `CustomerService`: Para eliminar un customer, usaremos nuestro nuevo query method, que devuelve `Mono<Boolean>`.
- `controller`
  - `CustomerController`: Corregimos el método de eliminado, ya que el service devuelve un `Mono<Boolean>`.

Otra forma de solucionar este problema hubiera sido, al igual que en `customerService.updateCustomer()`, buscar el id y si no lo encuentra, devolver NotFound.

## Paginated Results

Vamos a añadir un nuevo requerimiento.

Como parte del endpoint GET /customers, devolvemos un Flux<Customer>. Como tenemos 10 customers, todo va bien pero, ¿que pasaría si tuviéramos millones de customers en nuestra aplicación de producción?

Devolveríamos todos los customers en streaming. Si queremos esto, perfecto, ya lo tenemos, pero no es lo normal.

Lo normal es proveer resultados paginados, y lo indicamos como un nuevo endpoint que vamos a exponer.

![alt Paginated Results Endpoint](./images/14-PaginatedResultsEndpoint.png)

En `src/java/com/jmunoz/playground/sec03`, en los paquetes indicados modificamos las clases:

- `repository`
  - `CustomerRepository`: Creamos el método `findBy(Pageable pageable)`.
- `service`
  - `CustomerService`: Creamos el método `getAllCustomers(Integer page, Integer size)`.
- `controller`
  - `CustomerController`: Creamos el método `allCustomers(@RequestParam Integer page, @RequestParam Integer size)`

Ver `https://www.vinsguru.com/r2dbc-pagination/` para ver como devolver el número total de customers junto con ese Page results.

```java
@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Mono<Page<Product>> getProducts(PageRequest pageRequest){
        return this.repository.findAllBy(pageRequest)
                        .collectList()
                        .zipWith(this.repository.count())
                        .map(t -> new PageImpl<>(t.getT1(), pageRequest, t.getT2()));
    }

}
```

## CRUD APIs Demo

Vamos a hacer de nuevo pruebas, ejecutando nuestra app y usando Postman.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec03`.

Ahora ya podemos probar resultados paginados e ids no existentes que nos devuelven errores 404.

## WebTestClient - Introduction

En las próximas clases vamos a ver como hacer test a las APIs que hemos expuesto vía nuestro controller.

Para eso vamos a usar algo llamado `WebTestClient`.

- Ya hemos usado WebClient
  - Para enviar peticiones HTTP no bloqueantes.
- WebTestClient
  - Para escribir test unitarios/**integración**.

Particularmente, como parte de este curso estamos interesados en escribir tests de integración.

WebTestClient es muy fácil de usar:

- client.get()
  - post()
  - put()
  - delete()
- uri("/path")
- exchange() -> envía la request y obtiene el resultado
  - ¡Es bloqueante! ¡Es un test!

Primero indicaremos el verbo del endpoint, luego el path al que queremos enviar la petición y, por último se indica exchange().

Para un verbo POST, hay que indicar la data que se envía como parte del body.

Ejemplos:

```java
this.client.get()
    .uri("/path")
    .exchange();

this.client.post()
    .uri("/path")
    .bodyValue(object)
    .exchange();
```

- Tras exchange(), podemos hacer las aserciones
  - status / header / response content

Podemos hacer chaining.

Ejemplo:

```java
this.client.get()
    .uri("/customers/1")
    .exchange()
    .expectStatus().is2xxSuccessful()
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(CustomerDto.class)
    .value(dto -> ...);
```

- Incluso podemos usar `jsonPath`.
  - Muy útil cuando el contenido de la respuesta puede ser muy grande, con estructuras de datos anidadas muy complejas y no queremos validar todo el contenido, sino movernos a un path específico del JSON y validar solo es parte.
  - Ver `https://github.com/json-path/JsonPath`.

Ejemplo:

```java
this.client.get()
    .uri("/customers")
    .exchange()
    .expectStatus().is2xxSuccessful()
    .expectBody()
    .jsonPath("$.length()").isEqualTo(10)
    .jsonPath("$[0].name").isEqualTo("sam")
    .jsonPath("$[1].name").isEqualTo("mike")
```

## Integration Testing

Vamos a escribir tests de integración.

En `src/test/java/com/jmunoz/playground.tests.sec03` creamos la clase:

- `CustomerServiceTest`
  - Se hacen tests de integración.

Ejecutar los tests.

## POST / PUT - Body Publisher vs Body Value

Vamos a ver la diferencia entre `body()` y `bodyValue()`.

![alt Body vs BodyValue](./images/15-BodyvsBodyValue.png)

Usaremos `bodyValue()` cuando no tengamos un tipo publisher como Mono o Flux en memoria, sino, por ejemplo, un dto.

Usaremos `body()` para tipos de publisher Mono o Flux. Tenemos que indicar el tipo de lo que el publisher nos va a dar.

En test, usando `WebTestClient` casi siempre usaremos `bodyValue()`.

# Input Validation / Error Handling

## Problem Detail

En esta sección vamos a ver validaciones de entrada y gestión de errores.

Dada la aplicación realizada en el package `sec03` vamos a añadirle algunos requerimientos nuevos en `sec04`:

- CustomerDto debe venir informado name y email, este último con un formato válido.
- En vez de ResponseEntity, vamos a usar ControllerAdvice para manejar cualquier problema que queramos comunicarle al llamador.

Hemos dicho que Spring Framework se subscribe al tipo publisher, así que cuando emitimos una señal de error via el publisher, Spring Framework nos lleva automáticamente el ControllerAdvice y podemos devolver una respuesta de error apropiada.

En nuestra app, hasta ahora, cuando el customerId no se encuentra, sencillamente enviamos BadRequest.

Imaginemos aplicaciones grandes como GCP, AWS, Shopify... que exponen sus servicios via API y lo usan muchísimos desarrolladores.

No pueden sencillamente devolver BadRequest, tienen que indicar detalles de por qué la petición es errónea, como corregirla... al llamador.

Vamos a hablar de algo llamado `Problem Detail`:

- Es un formato estandarizado para comunicar los detalles de los errores al llamador.
  - Está basado en RFC7807 / RFC9457
  - Provee una estructura común para respuesta de errores.
  - El formato de respuesta es legible para la máquina como para humanos.

El standard de Problem Detail sugiere que la respuesta de error tenga estos campos:

![alt Problem Detail Fields](./images/16-ProblemDetailFields.png)

Ejemplos:

![alt Problem Detail Examples](./images/17-ProblemDetailExamples.png)

## What About Bean Validation?

Spring WebFlux soporta Bean Validation: `https://www.vinsguru.com/spring-webflux-validation/`. 

Pero no la vamos a usar en este curso.

Vamos a realizar nuestras propias validaciones sin usar esta dependencia porque así podemos aprender varias cosas.

## Project Setup

En `src/java/com/jmunoz/playground/sec04` creamos los paquetes y clases siguientes, copiados de `sec03` salvo los que se indiquen:

- `controller`
  - `CustomerController`
- `dto`
  - `CustomerDto`
- `entity`
    - `Customer`
- `mapper`
  - `EntityDtoMapper`
- `repository`
  - `CustomerRepository`
- `service`
  - `CustomerService`
- `advice`: Nuevo package
- `exceptions`: Nuevo package
- `validator`: Nuevo package

## Application Exceptions

Vamos a trabajar con clases de excepciones. Estos son los problemas que vamos a comunicar a nuestro llamador.

- Customer Not Found
- Invalid Input
  - name is required
  - valid email is required

En `src/java/com/jmunoz/playground/sec04` creamos clases en el siguiente paquete:

- `exceptions`
  - `CustomerNotFoundException`
  - `InvalidInputException`
  - `ApplicationExceptions`: Actúa como un Exception Factory.

## Request Validator

En `src/java/com/jmunoz/playground/sec04` creamos clases en el siguiente paquete:

- `validator`
  - `RequestValidator`: Tendremos aquí utility methods.

## Validation - Emitting Error Signal

En `src/java/com/jmunoz/playground/sec04` modificamos las siguientes clases del siguiente paquete:

- `controller`
    - `CustomerController`: Emitimos señal de error en vez de ResponseEntity si ocurre algún problema.

## @ControllerAdvice

En `src/java/com/jmunoz/playground/sec04` creamos clases en el siguiente paquete:

- `advice`
  - `ApplicationExceptionHandler`: En caso de cualquier señal de error, se dispara este método, basado en la clase de excepción.

## Demo via Postman

Modificamos `application.properties` para cambiar la property `sec=sec04` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec04`.

Ahora ya podemos probar validaciones y excepciones.

## Integration Testing

Vamos a escribir tests de integración.

En `src/test/java/com/jmunoz/playground.tests.sec04` creamos la clase:

- `CustomerServiceTest`
    - Se hacen tests de integración. Los happy path siguen igual y se da la respuesta de los objetos Problem Detail en caso de problemas.

Ejecutar los tests.

# WebFilter

## Introduction

WebFilter es un componente intermediario entre el servidor y el controller, tiene la habilidad de manipular peticiones entrantes y respuestas salientes.

Particularmente, es muy útil para manejar preocupaciones transversales como autenticación, autorización, logging, monitoreo, limitación de velocidad, etc.

Si añadimos un WebFilter, este se sitúa antes del controller.

![alt WebFilter](./images/18-WebFilter.png)

Es decir, cualquier petición que enviemos a nuestra aplicación, en vez de ir directamente al controller, ahora irá primero al WebFilter y luego al controller.

¿Cuál es el caso de uso? Imaginemos que tenemos un nuevo requerimiento por el que, cualquier petición a nuestra aplicación, debe tener esta cabecera concreta:

```java
@GetMapping
public Mono<ResponseEntity<List<CustomerDto>>> allCustomers(@RequestHeader(value = "auth-token", required = false) String token) {
    // validation
    if (Objects.isNull(token)) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    return this.customerService.getAllCustomers()
            .collectList()
            .map(ResponseEntity::ok);
}
```

En este controller se ha añadido la validación de token, pero aquí queda realmente feo. Imaginemos que tenemos 100 endpoints, tendríamos que repetir este código 100 veces.

Aquí es donde WebFilter es muy útil. Podemos hacer validaciones comunes antes de que la petición alcance al controller.

En WebFilter:

- No hacemos validaciones del body (si viene un mail con formato correcto...)
  - Esto es porque entre el WebFilter y el controller es cuando se deserializa el body de la petición.
  - Si se hiciese todo en un WebFilter, ¿para qué queremos clases de controller y de service? 
- Podemos acceder a
  - path
  - header
  - parameters
  - cookies
  - etc.

Recordar, solo para problemas transversales, es decir, validaciones muy genéricas irrespectivamente de la API que estamos invocando.

En `src/java/com/jmunoz/playground/sec05` creamos los paquetes y clases siguientes, copiados de `sec04` salvo los que se indiquen:

- `controller`
    - `CustomerController`
- `dto`
    - `CustomerDto`
- `entity`
    - `Customer`
- `mapper`
    - `EntityDtoMapper`
- `repository`
    - `CustomerRepository`
- `service`
    - `CustomerService`
- `advice`
    - `ApplicationExceptionHandler`
- `exceptions`
    - `CustomerNotFoundException`
    - `InvalidInputException`
    - `ApplicationExceptions`
- `validator`
    - `RequestValidator`
- `filter`: Nuevo package

## WebFilter Chain - How It Works

En una aplicación se pueden encadenar varias clases WebFilter para hacer varias validaciones antes de que la petición pase al controller.

![alt WebFilter Chain](./images/19-WebFilterChain.png)

El primer WebFilter recibe la petición y hará algún tipo de control (no siempre será una validación). Una vez ha terminado, la petición irá al siguiente WebFilter de la cadena, que hará su control y, si todo va bien, pasará la petición al siguiente WebFilter de la cadena... y así hasta que en el último WebFilter, si todo va bien, pase la petición al controller.

Para pasar de un WebFilter al siguiente de su cadena, hay que ejecutar `return chain.filter(exchange);`.

![alt WebFilter Chaining Example](./images/20-WebFilterChainingExample.png)

¿Cómo se establece el orden en la cadena de WebFilter? Se usa la anotación `@Order(1)`, `@Order(2)`..., a nivel de clase, en la imagen iría debajo de `@Service`.

Si algún WebFilter considera que debe rechazar la petición, esta no llega al siguiente WebFilter ni al controller.

## WebFilter - Authentication/Authorization Requirement!

Tenemos un nuevo requerimiento para implementar `autenticación` y `autorización`.

**Autenticación**

- Tenemos dos categorías de llamadores.
  - STANDARD
  - PRIME
- Se espera que todos los llamadores incluyan en el header un token de seguridad como parte de la petición.
  - "auth-token"
- El valor debe ser:
  - "secret123" => STANDARD
  - "secret456" => PRIME

Si no se cumple alguno de estos requerimientos de autenticación, rechazar la petición con código de estado 401.

**Autorización**

- Tenemos dos categorías de llamadores.
  - STANDARD
  - PRIME
- A los usuarios PRIME se les permite cualquier tipo de petición.
- A los usuarios STANDARD se les permite solo peticiones GET.

Si no se cumple alguno de estos requerimientos de autorización, rechazar la petición con código de estado 403.

## WebFilter Implementation

Trabajamos en el WebFilter de autenticación.

En `src/java/com/jmunoz/playground/sec05` creamos fuentes en el siguiente paquete:

- `filter`
  - `Category`: Es un enum con las categorías de llamadores permitidos.
  - `AuthenticationWebFilter`: Pasamos un atributo (ver siguiente clase) al siguiente WebFilter.

## Attributes via WebFilter

Trabajamos en el WebFilter de autorización.

![alt WebFilters Attributes](./images/21-WebFiltersAttributes.png)

Estos atributos son un mapa (pares de clave-valor). Un WebFilter puede proveer cierta información sobre la petición a otro WebFilter en la cadena usando este mapa.

Estos atributos pueden llegar al controller perfectamente, en caso necesario.

En `src/java/com/jmunoz/playground/sec05` creamos fuentes en el siguiente paquete:

- `filter`
  - `AuthorizationWebFilter`: Trabajamos con el atributo pasado desde `AuthenticationWebFilter`.

## WebFilter Demo

Vamos a probar nuestras APIs usando Postman.

Modificamos `application.properties` para cambiar la property `sec=sec05` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec05`.

En los headers de las peticiones se ha añadido la property `auth-token`. Si la petición no tiene esta property en su header, obtendremos un error `401 Unauthorized`.

Los valores de esta property solo pueden ser `secret123` para un usuario `STANDARD` o `secret456` para un usuario `PRIME`. Con cualquier otro valor, volveremos a obtener el error `401 Unauthorized`.

Para cualquier petición distinta de `GET`, el valor de la header property `auth-token` solo puede ser `secret456`, un usuario `PRIME`. En caso contrario obtendremos un error `403 Forbidden`.

## Accessing Attributes via @Controller

Vamos a ver como podemos acceder a los atributos añadidos a la petición en los WebFilter en el controller.

En los parámetros del método tenemos que indicar la anotación y el atributo al que queremos acceder, seguido del tipo y nombre del parámetro: `@RequestAttribute("category") Category category`

En `src/java/com/jmunoz/playground/sec05` modificamos la siguiente clase del siguiente paquete:

- `controller`
    - `CustomerController`: Accedemos al atributo establecido en el WebFilter.

Se puede probar abriendo Postman y ejecutando el endpoint `customers - get all`. Ir a la terminal donde se ejecuta el programa y se verá escrita la categoría en consola.

## Problem Detail - WebFilter Workaround

Como parte de nuestros WebFilter, hacemos algunas validaciones, establecemos el código HTTP Response apropiado y lo devolvemos.

Parece que todo funciona perfectamente. Pero, ¿cómo enviamos `ProblemDetail` tal y como hicimos en la sección anterior?

Actualmente, no se puede enviar una señal de error ni hacer un `throw` de una excepción en nuestros WebFilter y que lo recoja nuestro `Controller Advice`. Estaría genial tener centralizado solo un gestor de excepciones `ApplicationExceptionHandler` ya que es un buen patrón, pero, de nuevo, Spring WebFlux todavía no maneja esto.

**Puede cambiar en un futuro, por lo que conviene revisarlo.**

Entonces, necesitamos un gestor de excepciones global, y hay varias soluciones, pero ninguna es realmente tan elegante como la que ya tenemos.

Por tanto, si realmente, **REALMENTE**, necesitamos enviar `ProblemDetail` como parte de nuestro WebFilter, esta es la solución.

En `src/java/com/jmunoz/playground/sec05` creamos la siguiente clase del siguiente paquete:

- `filter`
    - `FilterErrorHandler`: Solo si realmente necesitamos añadir `ProblemDetail` como parte de lo que devuelve nuestro `WebFilter`.

Para poder probar esto, en `src/java/com/jmunoz/playground/sec05` modificamos la siguiente clase del siguiente paquete:

- `filter`
    - `AuthenticationWebFilter`: Modificado para ver si funciona el `ProblemDetail` añadido (se deja comentado)

Se puede probar ejecutando la aplicación, abriendo Postman y ejecutando el endpoint `customers - get-all ProblemDetail` que no tiene en el header la property `auth-token`.

## Integration Tests

Vamos a escribir tests de integración.

En `src/test/java/com/jmunoz/playground.tests.sec05` creamos la clase:

- `CustomerServiceTest`
    - Se hacen tests de integración de los WebFilter.

Ejecutar los tests.

# Functional Endpoints

## Introduction

Tradicionalmente, exponemos APIs via controllers anotados como este, que funcionan perfectamente:

![alt Controller Annotation](./images/22-ControllerAnnotation.png)

En este caso, anotado con `@GetMapping` y `@PathVariable` que, de alguna manera, Spring invocará cuando hagamos una petición GET. Luego, tomamos el control y llamaremos a la capa de servicio.

Además de estos controller anotados, WebFlux provee una solución alternativa para exponer APIs llamada `functional endpoints`.

![alt Functional Endpoints](./images/23-FunctionalEndpoints.png)

Consiste en exponer toda la lógica de routing en un estilo de programación funcional.

Si el path es `/customers` entonces ejecuta `getAllCustomers`... y también proveemos el gestor de errores en `onError(...)`.

Lo bueno es que, si queremos, podemos exponer APIs programáticamente.

Por tanto, cuando enrutamos, no vamos directamente a la capa de servicio, porque en la petición tenemos un objeto `server request`, de bajo nivel, y tendremos que extraer la información de la petición en esa capa amarilla de la imagen, que podríamos llamar `Request Handler`. Después, ya podemos pasar a la capa de servicio.

La ventaja de estos endpoints funcionales consiste en la flexibilidad que nos da.

Todo esto es opcional, podemos seguir usando nuestros controladores anotados como siempre.

## Router Configuration and Router Handler

Vamos a configurar el proyecto para que funcione con endpoints funcionales.

En `src/java/com/jmunoz/playground/sec06` creamos los paquetes y clases siguientes, copiados de `sec04` (si, la 4) salvo los que se indiquen:

- `dto`
    - `CustomerDto`
- `entity`
    - `Customer`
- `mapper`
    - `EntityDtoMapper`
- `repository`
    - `CustomerRepository`
- `service`
    - `CustomerService`
- `exceptions`
    - `CustomerNotFoundException`
    - `InvalidInputException`
    - `ApplicationExceptions`
- `validator`
    - `RequestValidator`
- `config`: Nuevo package
  - `RouterConfiguration`: Aquí exponemos todas nuestras funciones de router como un bean.
  - `CustomerRequestHandler`: Aquí extraemos los datos de la petición necesarios.

## Input Validation / Error Handler

Cuando los handler functions emiten una señal de error, tenemos que gestionarlo. Es algo similar al Controller Advice de la sección anterior pero para router functions.

En `src/java/com/jmunoz/playground/sec06` modificamos un fuente y creamos otro en el siguiente paquete:

- `config`
    - `RouterConfiguration`: Gestionamos las excepciones.
    - `ApplicationExceptionHanler`: Creamos Problem Detail y devolvemos Mono<ServerResponse> cuando hay excepción.

## Paginated Results

Añadimos el endpoint funcional para obtener los customers de manera paginada.

En `src/java/com/jmunoz/playground/sec06` modificamos fuentes en el siguiente paquete:

- `config`
    - `RouterConfiguration`: Añadimos la llamada al handler function para gestionar customers paginados.
    - `CustomerRequestHandler`: Gestionamos los datos de la petición y llamamos al servicio necesario para obtener customers paginados.

## Order of the routes

Tenemos que tener cuidado cuando trabajemos con RouterFunctions.

Cuando obtenemos la petición GET, basado en el método de la petición HTTP y el patrón del path, RouterFunctions intentará asignarlo a un Request Handler apropiado.

Pero en este ejemplo, tendremos un error si queremos acceder a `/customers/paginated`:

```java
        return RouterFunctions.route()
                .GET("/customers", this.customerRequestHandler::allCustomers)
                .GET("/customers/{id}", this.customerRequestHandler::getCustomers)
                .GET("/customers/paginated", this.customerRequestHandler::paginatedCustomers)
                .build();
```

El problema es que selecciona el endpoint `/customers/{id}` por ser un path variable.

Para evitar esto, tenemos que tener cuidado con el orden en que indicamos los paths routes. El orden correcto sería este:

```java
        return RouterFunctions.route()
                .GET("/customers", this.customerRequestHandler::allCustomers)
                .GET("/customers/paginated", this.customerRequestHandler::paginatedCustomers)
                .GET("/customers/{id}", this.customerRequestHandler::getCustomers)
                .build();
```

## Demo via Postman

Vamos a probar nuestras APIs usando Postman.

Modificamos `application.properties` para cambiar la property `sec=sec06` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec06`.

## Integration Tests

Vamos a escribir tests de integración.

En `src/test/java/com/jmunoz/playground.tests.sec06` creamos la clase:

- `CustomerServiceTest`
    - Se hacen tests de integración de los Functional Endpoints.

Ejecutar los tests.

## Multiple Router Functions

En estas clases veremos detalles menores, pero importantes, que tenemos que tener en cuenta.

En `src/java/com/jmunoz/playground/sec06` creamos el siguiente fuente de prácticas en el siguiente paquete:

- `config`
    - `RouterConfigurationPractices`: Creamos varios métodos RouterFunction para no tener tantos endpoints en un solo RouterFunctions, todos los métodos expuestos como beans.

En esta clase tenemos varios endpoints, pero son pocos. En una aplicación real podríamos tener 50 o 100 endpoints.

No es necesario crear un solo método RouterFunction y exponerlo todo, sino que podemos tener varios métodos RouterFunction si queremos.

Ejecutamos nuestra app `WebfluxPlaygroundApplication` y probamos en Postman que todo sigue funcionando exáctamente igual.

## Nested Router Functions

Cuando tenemos varios endpoints, no tiene por qué ser necesario exponer varios beans.

Podemos tener un router a un nivel alto que puede enrutar a las funciones router hijas, como un tipo de lógica de enrutamiento anidada basada en rutas.

En `src/java/com/jmunoz/playground/sec06` modificamos el fuente de prácticas en el siguiente paquete:

- `config`
    - `RouterConfigurationPractices`: Los métodos no tienen por qué exponerse todos como beans, sino que hay un método padre que se expone como bean. 

Escoger entre varias funciones router o funciones router anidadas es solo cuestión de como se quiere organizar todo.

Ejecutamos nuestra app `WebfluxPlaygroundApplication` y probamos en Postman que todo sigue funcionando exáctamente igual.

## What About WebFilters

Si queremos trabajar con el enfoque de Router Functions, esta clase nos enseña a tener WebFilter.

Los WebFilter, aunque se han añadido al final en la clase de ejemplo, es lo primero que se ejecuta.

En `src/java/com/jmunoz/playground/sec06` modificamos el fuente de prácticas en el siguiente paquete:

- `config`
    - `RouterConfigurationPractices`: Añadimos WebFilter usando el método `filter()`.

Ejecutamos nuestra app `WebfluxPlaygroundApplication` y probamos en Postman que todo sigue funcionando exáctamente igual.

## Request Predicates

En los métodos `GET`, `POST`, etc., hemos trabajado con la firma en la que indicamos un patrón y la correspondiente función handler.

![alt Request Predicates](./images/24-RequestPredicates.png)

La tercera firma indica que, da igual el path, se va a ejecutar esa función handler.

Pero, donde apunta la flecha vemos que también podemos indicar una RequestPredicate y una correspondiente función handler.

Sabemos que un `predicate` consiste en pasar un objeto y se devuelve true o false. Como parte de un `RequestPredicate` lo que hacemos es pasar la request, con lo que tenemos acceso a todo el request (path variable, query parameters, headers, cookies...) y tenemos que devolver true o false. Si es true se ejecuta la función handler indicada y si es false va al siguiente handler.

La cuarta firma vemos que utiliza un patrón, un predicate y una función handler. Solo si el predicate devuelve true se ejecutará su función handler.

Estos usos de predicate son muy útiles cuando solo un patrón no es suficiente, por ejemplo, para invocar el endpoint `GET All Customers` puede que existan propiedades en el header que tenemos que mirar usando el predicate, para ver si existen y, solo entonces, ejecutar ese función handler.

`RequestPredicate` es una interface. Existe también una clase de utilidad `RequestPredicates` que sirve para poder crear un `RequestPredicate` rápidamente.

En `src/java/com/jmunoz/playground/sec06` modificamos el fuente en el siguiente paquete:

- `config`
    - `RouterConfiguration`: Usamos `RequestPredicate` para enrutar paths a handler functions.

Descomentar la parte de RequestPredicate para probar.

Ejecutamos nuestra app `WebfluxPlaygroundApplication` y probamos en Postman `customers - get-by-id`, tanto con el id 5 como el 1 y el 10. Vemos que solo funcionará el id con valor 10.

## Request Predicate - Calculator Assignment

Vamos a realizar este ejercicio usando Request Predicate.

![alt Request Predicate Assignment](./images/25-RequestPredicateAssignment.png)

Lo de menos es la calculadora. Lo importante es jugar con Request Predicate.

En `src/java/com/jmunoz/playground/sec06` creamos el fuente en el siguiente paquete:

- `assignment`: Nuevo package
  - `CalculatorAssignment`: Es un ejercicio para jugar con Request Predicate.

Vamos a probar nuestras APIs usando Postman.

Modificamos `application.properties` para cambiar la property `sec=sec06` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec06/assignment`.

Vamos a escribir tests de integración.

En `src/test/java/com/jmunoz/playground.tests.sec06` creamos la clase:

- `CalculatorTest`
    - Se hacen tests de integración de `CalculatorAssignment`.

Ejecutar los tests.

# WebClient

## Introduction

Aunque ya usamos WebClient en la sección 1, ahora vamos a discutirla en detalle.

WebClient es sencillamente una plantilla REST Reactiva.

- API fluida basada en Reactor para hacer peticiones HTTP.
    - Envoltorio alrededor de reactor-netty
- Recibe la respuesta de manera No bloqueante
- Inmutable
- Thread safe

Esta imagen muestra un ejemplo de una app que hace dos llamadas usando WebClient.

![alt WebClient Example](./images/26-WebClientExample.png)

WebClient, una vez construido, es inmutable. Sin embargo, si tenemos que modificar cualquier configuración por defecto que hayamos hecho, podemos usar `oldClient.mutate()`, como se ve en esta imagen.

![alt WebClient Mutate](./images/27-WebClientMutate.png)

Con esto creamos un nuevo builder y podemos modificar las configuraciones que deseemos para crear un nuevo client.

Así es como vamos a usar WebClient:

![alt WebClient Use](./images/28-WebClientUse.png)

Es muy parecido a `WebTestClient`, que ya hemos visto.

Una vez ejecutado `retrieve()` obtenemos la respuesta, y, usando `bodyToMono(...)` proveemos el tipo, en la imagen `Product.class`, es decir, decodificamos la respuesta a ese tipo.

Si esperáramos varios productos en streaming, podíamos haber indicado `bodyToFlux(...)`.

Una vez tenemos `bodyToMono(...)` ya lo que indiquemos es básicamente un pipeline reactivo.

En la imagen, en el método `post()` vemos `bodyToValue(...)`. Recordar que se usa `bodyToValue(...)` para enviar un objeto que tenemos en memoria.

Para trabajar con `WebClient` vamos a necesitar una aplicación con la que interactuar. Esta será `external-services.jar`.

Para ejecutarla, solo tenemos que acceder a una terminal y ejecutar: `java -jar external-services.jar`.

Luego, acceder en el navegador al Swagger de este servicio: `http://localhost:7070/webjars/swagger-ui/index.html`. Vamos a usar `demo02`.

![alt Demo02](./images/29-WebClientDemo02.png)

## Project Setup

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `AbstractWebClient`
  - Clase abstracta que será extendida por varias clases. Aquí tendremos métodos de utilidad.

**IMPORTANTE:** No son tests. Son clases hechas para poder practicar WebClient.

## Simple GET

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec01MonoTest`
  - Vamos a realizar una petición GET sencilla.
  - Vamos a trabajar con el endpoint GET `/demo02/lec01/product/{id}`
  - El id va de 1 a 100 y cada call le lleva 1 segundo responderlo.
  - Método `simpleGet()`
- `dto`: Nuevo package
  - `Product`: Es un record.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Los resultados pueden verse tanto en la terminal donde ejecutamos `external-services.jar`, donde se ve la petición recibida y si se ha completado o ha dado error, como en la terminal donde ejecutamos los tests, donde vemos el resultado de la petición.

## Non-blocking Concurrent Requests

En esta clase vamos a probar como podemos enviar una petición concurrente no bloqueante usando WebClient.

Esto ya lo vimos en el curso anterior: `https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming/tree/main/01-reactive-programming-playground#non-bloking-io-client`

- WebClient es un wrapper alrededor de Reactor Netty.
  - Usa 1 thread / CPU
  - Es no bloqueante

¡Solo 1 thread hace el trabajo!

En `src/test/java/com/jmunoz/playground.tests.sec07` seguimos trabajando con la clase:

- `Lec01MonoTest`
  - Método `concurrentRequests()`

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Los resultados pueden verse tanto en la terminal donde ejecutamos `external-services.jar`, donde se ve la petición recibida y si se ha completado o ha dado error, como en la terminal donde ejecutamos los tests, donde vemos el resultado de la petición.

## How Event Loop Works

Ver `https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming/tree/main/01-reactive-programming-playground#como-funciona-event-loop`.

## URI Variables

En nuestros ejemplos hemos construido la URI haciendo una concatenación de Strings, cosa lógica porque nuestra URI es muy sencilla.

```java
this.client.get()
        .uri("/lec01/product/" + i)
        .retrieve()
        .bodyToMono(Product.class)
```

Pero en la vida real podemos tener URL mucho más largas, y la concatenación de Strings se verá muy fea.

Para solucionar esto, tenemos sobrecarga de métodos disponibles:

```java
Uri uri

String uri, Object... uriVariables

String uri, Map<String, ?> uriVariables
```

Y el resultado sería este:

```java
// Usando variables
this.client.get()
        .uri("/lec01/product/{id}", i)
        .retrieve()
        .bodyToMono(Product.class)

this.client.get()
        .uri("/{lec}/product/{id}", "lec01", i)
        .retrieve()
        .bodyToMono(Product.class)

// Variables con map
var map = Map.of(
        "lec", "lec01",
        "id", 1
);

this.client.get()
        .uri("/{lec}/product/{id}", map)
        .retrieve()
        .bodyToMono(Product.class)
```

En `src/test/java/com/jmunoz/playground.tests.sec07` seguimos trabajando con la clase:

- `Lec01MonoTest`
    - Modificamos el método `concurrentRequests()` para usar variables en la URI.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Los resultados pueden verse tanto en la terminal donde ejecutamos `external-services.jar`, donde se ve la petición recibida y si se ha completado o ha dado error, como en la terminal donde ejecutamos los tests, donde vemos el resultado de la petición.

## Streaming GET

Vamos a hacer pruebas con respuestas en stream.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec02/product/stream`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec02FluxTest`
    - Vamos a realizar una petición GET que devuelve una respuesta streaming.
    - Vamos a trabajar con el endpoint `/demo02/lec02/product/stream`
    - Este endpoint provee 10 productos como stream, con 500ms de delay entre cada uno de ellos. No es necesario pasar ningún id. 

## POST - Body Publisher vs Body Value

En esta clase vemos la diferencia entre usar bodyValue o body.

- bodyValue - Lo usaremos si tenemos un objeto o un DTO en MEMORIA (esto es lo importante, que esté en memoria)
- body - Lo usaremos si tenemos un tipo Publisher.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint POST `/demo02/lec03/product`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec03PostTest`
    - Vamos a trabajar con el endpoint `/demo02/lec03/product`
    - Este endpoint acepta una petición POST para un producto y le lleva 1 segundo responder.

## Default Headers Configuration/Override

Vamos a ver como configurar el header, sobreescribirlo...

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec04/product/{id}`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec04HeaderTest`
    - Vamos a trabajar con el endpoint `/demo02/lec04/product/{id}`
    - Este endpoint espera un header con la propiedad `caller-id`, es decir, estaríamos indicando quién llama al servicio. Si no viene este header, no enviaremos la respuesta.

## Remote Service - Error Handling

Vamos a ver como decodificar las respuestas de error, para poder manejar los errores.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec05/calculator/{first}/{second}`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec05ErrorResponseTest`
    - Vamos a trabajar con el endpoint `/demo02/lec05/calculator/{first}/{second}`
    - Hace la operación matemática y devuelve el resultado. Ambos parámetros deben ser mayores que cero. En el header mandamos `operación` y alguna de estas operaciones como valor `+, -, *, /`. En caso contrario la operación fallará con status 404 y devolverá un objeto ProblemDetail.
- `dto`: Package
    - `CalculatorResponse`: Es un record.

## Retrieve vs exchange

Hasta ahora hemos usado `retrieve()`.

Usaremos `exchange()` si necesitamos acceder a las `response headers`, `cookies`, `status code`, etc., es decir a niveles de detalle de bajo nivel porque necesitemos más control. Notar en la imagen que obtenemos `clientResponse` que contiene todos esos valores de bajo nivel, y podemos hacer y devolver lo que queramos al pipeline reactivo.

![alt Retrieve vs Exchange](./images/30-RetrieveVsExchange.png)

En `src/test/java/com/jmunoz/playground.tests.sec07` seguimos usando la clase:

- `Lec05ErrorResponseTest`
    - Trabajamos con `exchange()`

## Query Params

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec06/calculator`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec06QueryParamsTest`
    - Vamos a trabajar con el endpoint `/demo02/lec06/calculator`
    - Es lo mismo que hemos visto en `Lec05ErrorResponseTest` pero en vez de usar `Path Variables` y un header, usamos `Query Parameters`.

## WebClient - Basic Auth

Vamos a ver como configurar credenciales Basic Auth usando WebClient.

Los desarrolladores suelen usar Basic Auth para autenticación de servicio a servicio.

![alt Basic Auth](./images/31-BasicAuth.png)

En este caso, enviamos credenciales desde `Order Service` a `Payment Service` para poder usar el servicio.

Para este ejemplo, estas credenciales están codificadas en Base64.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec07/product/{id}`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec07BasicAuthTest`
    - Vamos a trabajar con el endpoint `/demo02/lec07/product/{id}`
    - El valor de id puede ir de 1 a 100.
    - Si no se envían las credenciales `username: java, password: secret` devuelve 401.

## Bearer Auth

Vamos a ver como enviar un bearer token en un WebClient.

Esto es muy usado.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec08/product/{id}`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec08BearerAuthTest`
    - Vamos a trabajar con el endpoint `/demo02/lec08/product/{id}`
    - El valor de id puede ir de 1 a 100.
    - Si no se envía el bearer token `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9` devuelve 401.

## Exchange Filter Function

Es muy similar a WebFilter, pero WebFilter funciona para una solicitud entrante.

![alt Exchange Filter Function](./images/32-ExchangeFilterFunction.png)

Similar a WebFlux, WebClient es responsable de gestionar la solicitud saliente, y tiene sus propios filtros, a los que se les llama Exchange Filter Function.

Usando estos Exchange Filter Function, podemos manejar preocupaciones transversales similares pero relacionadas con las solicitudes salientes, como asegurar que el token se establece antes de enviar una solicitud al servicio remoto, logging, monitoreo, manejo de errores, etc. 

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec09/product/{id}`.

En `src/test/java/com/jmunoz/playground.tests.sec07` creamos la clase:

- `Lec089ExchangeFilterTest`
    - Vamos a trabajar con el endpoint `/demo02/lec09/product/{id}`
    - El valor de id puede ir de 1 a 100.
    - Espera que se envíe un nuevo bearer token `Authorization: Bearer [generate new token]` cada vez.
    - Para enviar un nuevo token se usa `UUID.randomUUID().toString().replace("-", "")`.
    - También creamos otro Filter Function para añadir funcionalidad de logging.

Como tenemos que generar un token como parte de cada solicitud, la configuración de default headers no nos va a funcionar, ya que el token es uno concreto y además, en esta instrucción:

`private final WebClient client = createWebClient(b -> b.defaultHeaders(h -> h.setBearerAuth("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")));`

Estamos creando un bean, el que genera `createWebClient()`, y puede estar inyectándose ese WebClient es muchas clases de servicio.

Aquí es donde funcionan muy bien las Exchange Filter Function.

Exchange Filter Function es una interfaz funcional de este tipo: `Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next);`.

Vemos que tenemos la solicitud del cliente antes de que se envíe al servicio remoto, y podemos modificarla.

Usando la función `next` daremos la solicitud a la siguiente Exchange Filter Function que se encuentre en la cadena.

Devolvemos `Mono<ClientResponse>`

Toda esta parte funciona igual que WebFilter.

## WebClient - Attributes

En WebClient podemos tener varias funciones de filtro (Filter Function).

Una función de filtro puede pasar información sobre la petición a otra función de filtro usando atributos.

![alt WebClient Attributes](./images/33-WebClientAttributes.png)

También vemos en la imagen que pueden pasarse estos atributos desde las clases de servicio.

Por ejemplo, una clase de servicio puede añadir la funcionalidad de logging, pero la otra clase de servicio puede no querer esta funcionalidad de logging.

Usando estos atributos podemos controlar como van a trabajar estas funciones de filtro.

Para ejecutar las pruebas no olvidar primero ejecutar `external-services.jar`. Vamos a trabajar con el endpoint GET `/demo02/lec09/product/{id}`.

En `src/test/java/com/jmunoz/playground.tests.sec07` modificamos la clase:

- `Lec089ExchangeFilterTest`
  - Añadimos atributos a nuestra funcionalidad de logger. Nuestra clase de servicio va a enviar un flag para habilitar o no este logging.

Lo importante aquí es entender que podemos tener funciones o clases de utilidad reutilizables que podemos adjuntar al WebClient (los filtros), pero que podemos habilitarlos/deshabilitarlos, o enviarles cualquier información, utilizando atributos.

# Streaming

## Introduction

![alt Communication Patterns](./images/34-CommunicationPatterns.png)

Tradicionalmente, todos hemos usado el estilo de comunicación `request -> response`. Enviamos una petición al servidor backend y este nos devuelve una respuesta.

Sin embargo, usando el tipo Publisher Flux, accedemos a patrones de comunicación adicionales usando el modelo reactivo WebFlux:

- `request -> streaming response`: El cliente envía una petición al servidor backend y el backend provee varias respuestas en streaming de vuelta al cliente.
  - Hay muchos casos de uso para este modelo de comunicación, por ejemplo una descarga de fichero, o cuando reservamos un Uber, y desde su app nos van mandando mensajes indicando lo que queda para que llegue el conductor.
  - La experiencia de usuario mejora mucho con este patrón de comunicación, porque el cliente no tiene que estar enviando la petición una y otra vez al servidor backend, sino que es el servidor el que va proveyendo actualizaciones periódicas.
- `streaming request -> response`: El cliente envía peticiones en streaming al servidor backend y el backend server devuelve una sola respuesta al cliente.
  - Ejemplos de casos de uso serían, por ejemplo, una subida de fichero o de data, como cuando desde un reloj inteligente Apple emitimos las pulsaciones del corazón al servidor de Apple en formato de streaming.
- `bidirectional streaming`: Tenemos streaming en ambos lados. Es una combinación de los dos patrones de comunicación anteriores.
  - Ejemplos de casos de uso serían aplicaciones interactivas como juegos online, un chat...

Todo esto ya lo vimos aquí `https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming?tab=readme-ov-file#por-qu%C3%A9-no-usar-virtual-threads`.

## Uploading Million Products - Usecase

Cuando hablamos de streaming, nos vamos a centrar en `servicio => servicio`, es decir, comunicación backend entre servicios.

Vamos a hacer un ejemplo: Es una prueba de concepto para usar una de las mayores compañías e-commerce. El caso de uso es subir millones de productos en su plataforma.

Esta aplicación de terceros tiene una API que expone un POST para crear un producto. ¿Debemos usar esa API para llamar una y otra vez, millones de veces? Estos servicios suelen permitir subir productos de 10 en 10, y vamos a tener que esperar para poder mandar los siguientes 10.

Además de exponer la API POST, también tienen un servicio para poder subir ficheros CSV, pero esto también plantea muchos problemas.

Aquí quedan resumidas las posibilidades que tenemos y sus inconvenientes:

- POST
  - Se incrementa el tráfico / latencia de red
  - Tiempo de espera innecesario
  - Validaciones redundantes
- Fichero de subida CSV
  - El fichero puede estar corrupto
  - Escapar "," en los comentarios de los productos
  - Compleja estructura de datos JSON anidada, imposible de representar usando CSV

La técnica de `streaming` tiene muchas ventajas y puede ofrecer una mejor experiencia de usuario para nuestros clientes:

- Establecemos conexión UNA VEZ y enviamos mensajes en modo streaming, sin tener que preocuparnos de peticiones en paralelo
- No necesitamos que la request anterior se complete
- Se reduce el tráfico / latencia de red
- Podemos usar JSON para crear un product / item

## JSON Lines

Vamos a hablar del formato JSON.

![alt JSON Array](./images/35-JSONArray.png)

Lo que vemos en la imagen es un formato JSON Array, que tiene un problema. Digamos que hacemos una petición GET y obtenemos los primeros 1000 productos.

La respuesta comienza con `[` seguido del primer producto, luego el segundo... hasta el producto mil y por último terminamos con `]`.

Imaginemos que durante la respuesta, en el producto 999 el server falla y no puede proveer `]`. Si el cliente no puede recibir el corchete de cierre, no puede parsear la data, es decir, la data está corrupta.

Esto es muy similar a todo o nada.

Un Array JSON es bueno para sets de datos pequeños, pero no para sets de datos muy grandes.

Imaginemos que recuperamos millones de productos y que la data está bien, termina con el corchete.

Ahora el parser tiene que leerlo todo, manteniendo toda la data en memoria y, solo entonces, podrá parsear la data. Esto puede ser un problema, por ejemplo, por falta de memoria.

Aquí es donde aparece el formato `JSON Line`.

![alt JSON Line](./images/36-JSONLine.png)

- También conocido como ND-JSON
  - ND = new-line delimited
- Cada línea es un objeto JSON válido que representa un producto
  - Autocontenido
  - Fácil de parsear
    - Sin necesidad de tener que leer todo y mantenerlo en memoria
    - Leemos línea a línea y se parsean independientemente
  - Muy bueno para streaming
  - Para sets de datos masivos
- Un JSON Array sigue siendo bueno para data pequeña y relacionada, por ejemplo: reseñas de un producto

## Project Setup

En esta clase vamos a configurar nuestro proyecto para jugar con streaming.

En esta sección vamos a jugar con la creación, subida, descarga... de millones de productos.

En `src/java/com/jmunoz/playground/sec08` creamos los paquetes y clases siguientes:

- `entity`
    - `Product`
- `dto`
    - `ProductDto`: Es un record
- `repository`
    - `ProductRepository`
- `mapper`
    - `EntityDtoMapper`

## Product Service

En `src/java/com/jmunoz/playground/sec08` creamos los paquetes y clases siguientes:

- `service`
  - `ProductService`

## Product Streaming Upload API

En `src/java/com/jmunoz/playground/sec08` creamos los paquetes y clases siguientes:

- `controller`
    - `ProductController`: Expone Product Upload API y devuelve el Dto UploadResponse. Notar `consumes = MediaType.APPLICATION_NDJSON_VALUE` porque usamos ND JSON.
- `dto`
    - `UploadResponse`: Es un record

## Product Client

Necesitamos una aplicación cliente que invoque la API para enviar los productos.

En `src/test/java/com/jmunoz/playground.tests.sec08` creamos la clase:

- `ProductClient`: Invoca la API y envía los productos

## Client Streaming Request - Demo

En `src/test/java/com/jmunoz/playground.tests.sec08` creamos la clase:

- `ProductsUploadDownloadTest`: Para hacer la demo tanto de upload como de download (lo codificamos más adelante)

Antes de ejecutar, modificamos `application.properties` para cambiar la property `sec=sec08` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Por último, ejecutamos nuestro cliente `ProductsUploadDownloadTest`, método `upload()`.

## @RequestBody - Non-Blocking Clarification

![alt CustomerDto vs Mono<CustomerDto>](./images/37-CustomerDtoVsMonoCustomerDto.png)

En la imagen vemos la diferencia entre usar CustomerDto (arriba) y Mono<CustomerDto> (abajo) en `@RequestBody`. 

En el recuadro de arriba, Spring esperará a que llegue `@RequestBody` para poder serializarlo. Es en el recuadro de abajo donde se usa un `@RequestBody` de una forma no bloqueante.

En esta segunda imagen vemos lo que ha ocurrido en la demo de la clase anterior.

![alt Flux<ProductDto>](./images/38-FluxProductDto.png)

Cuando un cliente quiere subir un producto, manda la petición POST. No hay un body, solo el tipo publisher.

Establecemos la conexión y Spring la acepta, asumiendo que vamos a obtener las peticiones en streaming porque el body de la petición es un Flux, así que el controller lo pasa a la capa de servicio.

Pero se ejecuta el controller, porque se puede ver el log con el texto `invoked`.

Ahora el cliente usa esta conexión y manda 10 productos, cada 2sg un body de un producto, en streaming, pero el texto `invoked`, como se ha dicho, solo se imprime una vez.

Esto significa que la petición POST solo se invoca una vez, pero, usando la conexión, el cliente puede enviar muchos mensajes en streaming, por lo que es posible subir esos 10 productos. De hecho, con solo esa conexión, podemos enviar millones de productos. 

## 1 Million Products Upload - Demo

En `src/java/com/jmunoz/playground/sec08` modificamos la clase:

- `controller`
    - `ProductController`: Para la prueba del millón de subidas de productos, quitamos el log que hace que se escriba cada uno de los productos.

En `src/test/java/com/jmunoz/playground.tests.sec08` modificamos la clase:

- `ProductsUploadDownloadTest`: Hacemos la subida de 1 millón de productos.

Antes de ejecutar, modificamos `application.properties` para cambiar la property `sec=sec08` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Por último, ejecutamos nuestro cliente `ProductsUploadDownloadTest`, método `upload()`.

Vemos que tarda 13sg en crear un millón de productos. Es importante recalcar que esto ocurre en una BD H2. En una BD Postgres va a tardar algo más, sobre unos 5 minutos, debido a las escrituras en disco y a la sobrecarga debido a la transaccionalidad. 

## Download API

En `src/java/com/jmunoz/playground/sec08` modificamos las clases siguientes:

- `controller`
    - `ProductController`: Añadimos la API para hacer la descarga de productos. Notar `produces = MediaType.APPLICATION_NDJSON_VALUE`.
- `service`
    - `ProductService`: Añadimos el método para hacer la descarga de productos.
  
En `src/test/java/com/jmunoz/playground.tests.sec08` modificamos las clases:

- `ProductClient`: Invoca la API `downloadProducts()` y recoge los productos.

Los productos no se van a imprimir porque son un millón, sino que se guardan en fichero. Ver clase siguiente.

## 1 Million Products Download - Demo

En `src/test/java/com/jmunoz/playground.tests.sec08` creamos/modificamos las clases:

- `FileWriter`: Guardamos el millón de productos en fichero en la raiz del proyecto con nombre `products.txt`.
- `ProductsUploadDownloadTest`: Añadimos el método `download()`.

Antes de ejecutar, modificamos `application.properties` para cambiar la property `sec=sec08` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Por último, ejecutamos nuestro cliente `ProductsUploadDownloadTest`, método `download()`.

Tarda 8sg en escribir en fichero el millón de productos.

## What About Bidirectional Stream

Hemos visto los patrones de comunicación `client streaming` con la subida de ficheros y `server streaming` con la descarga de ficheros.

Nos falta `bidirectional streaming`, que es una combinación de los dos anteriores.

Por ejemplo, en `ProductController`, en el método `upload()` enviamos una respuesta una vez se ha realizado la petición.

Si hacemos esto:

```java
return this.service.saveProducts(flux);
```

Estaríamos devolviendo un Flux<ProductDto`, con lo cual ya lo hemos transformado en un stream bidireccional.

# Server Sent Events / SSE

Vamos a hablar de los Server Sent Events, también conocidos como SSE o EventSource.

## Introduction

Comprendamos primero el problema.

![alt SSE Qué resuelve](./images/39-SSEWhatResolves.png)

Consideremos una plataforma de trading o un sitio de noticias que muestra resultados de elecciones, o un videojuego del que queremos saber las puntuaciones.

Vemos que hay mucha gente que se conecta al servidor, vía un navegador, a la vez. En esos casos, para proveer la última puntuación, o el último stock price o el último conteo de una votación, el frontend tiene que estar llamando una y otra vez al backend, preguntando por si tiene alguna actualización.

Si hay alguna actualización, entonces se mostrará en el navegador, pero si no, tener que estar llamando cada segundo o cada 5 segundos al backend es una pérdida de tiempo y de recursos.

Aquí es donde Server Sent Events es de mucha ayuda.

![alt SSE Qué es](./images/40-SSEWhatIs.png)

- Los Server Sent Events son un mecanismo por el cual el servidor backend puede hacer push de eventos en stream al frontend cuando hay alguna actualización.
- Es una comunicación de una vía porque el navegador NO tiene que llamar al backend para preguntar si hay actualizaciones.
  - Es el servidor mismo el que enviará la data cuando haya actualizaciones.
  - Por eso es una comunicación de una vía desde el backend al frontend.

Implementar estos servidores y eventos usando WebFlux es muy fácil. Como tenemos que hacer stream de mensajes al frontend, tenemos que devolver un Flux proveyendo `MediaType.TEXT_EVENT_STREAM_VALUE`.

Aquí mostramos los tipos de MediaType que hemos visto. El primero es para comunicación de servicio a servicio, mientras que el segundo es el MediaType que utilizaremos para una comunicación con el navegador.

Esto es porque los navegadores tienen su propia especificación. Esperan que los mensajes estén codificados en un formato específico, o no lo entenderán.

```java
// Flux<T>
MediaType.APPLICATION_NDJSON_VALUE; // service to service
MediaType.TEXT_EVENT_STREAM_VALUE; // to browser
```

Para jugar con los SSE, vamos a hacer uso de nuestro `ProductService`.

Vamos a asumir que nuestros usuarios están interesados en nuevos productos, así que esperan que el servidor backend les notifique cuando se añadan nuevos productos.

Dado este caso de uso, vamos a ver como se implementa usando Server Sent Events.

La parte más difícil de ver de este requerimiento de negocio es, ¿cómo podemos enviar un mensaje cuando se añade un nuevo producto?, y ¿cómo sabemos cuando se ha añadido un nuevo producto?

Para esto vamos a hacer uso de un concepto llamado `sinks`. Un Sink es algo que actúa a la vez como un publisher y un subscriber.

![alt Sinks](./images/41-Sinks.png)

En la imagen vemos como varios threads en la parte izquierda pueden emitir data mientras en el final de la parte derecha se actúa como un Flux a través del cual los subscribers pueden observar los items.

Esto es lo que vamos a usar para crear un stream de un nuevo producto.

## Sinks Configuration

En `src/java/com/jmunoz/playground/sec09` creamos los paquetes y clases siguientes, copiados de `sec08` salvo los que se indiquen:

- `entity`
    - `Product`
- `dto`
    - `ProductDto`: Es un record
    - `UploadResponse`: Es un record
- `repository`
    - `ProductRepository`
- `mapper`
    - `EntityDtoMapper`
- `service`
    - `ProductService`
- `controller`
    - `ProductController`
- `config`: Nuevo package
  - `ApplicationConfig`: Exponemos un bean para Sinks donde emitimos `ProductDto`.

## Emitting Items via Sink

En `src/java/com/jmunoz/playground/sec09` modificamos un fuente en el siguiente paquete:

- `service`
  - `ProductService`: Añadimos nuestro sink al servicio.

## Exposing Streaming API

En `src/java/com/jmunoz/playground/sec09` modificamos un fuente en el siguiente paquete:

- `controller`
    - `ProductController`: Notar `produces = MediaType.TEXT_EVENT_STREAM_VALUE`.

## SSE Demo

Vamos a probar lo que tenemos y en las siguientes clases vamos a añadir otro requerimiento.

Antes de ejecutar, modificamos `application.properties` para cambiar la property `sec=sec09` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Vamos al navegador, a la ruta `http://localhost:8080/products/stream`. No veremos nada porque no tenemos ningún producto.

Abrimos Postman e importamos el archivo incluido en la carpeta `postman/sec09` para crear productos..

En cuanto creemos un producto usando Postman, a la vez en el navegador aparecerá ese producto, es decir, estamos notificando al usuario un nuevo producto.

Cerramos el navegador y abrimos un terminal. Ejecutar `curl http://localhost:8080/products/stream`. Veremos el último producto creado, es decir, si creamos 5 productos, nos aparecerá solo el 5.

## Price Filter Implementation

Vamos a añadir un par de requerimientos a nuestra app:

- Añadimos un producto cada 1 seg.
- Los usuarios quieren ver los nuevos productos mostrados en formato tabla dentro de la interfaz del navegador.
  - Para la interfaz de usuario, usaremos HTML y Vanilla JS. Esto no es importante.
- Los usuarios necesitan la posibilidad de filtrar productos basada en el precio.
  - Unos usuarios solo querrán ser notificados si el producto es menor que 50, y otros si es menor que 80...

En `src/java/com/jmunoz/playground/sec09` creamos/modificamos fuentes en los siguientes paquetes:

- `service`
    - `DataSetupService`: Añade un producto cada sg. Se ejecuta automáticamente al ejecutarse la app.
- `controller`
    - `ProductController`: Añadimos el filtro basado en el precio.

## Adding UI - Index HTML

En `src/java/resources/static` creamos el siguiente archivo html.

- `index.html`: Es la UI de nuestra app del paquete `sec09`.

Cuando pulsamos el botón `Notify me!!` empezamos a observar productos filtrados por el precio indicado en la caja de texto.

## Price Filter Demo via UI

Antes de ejecutar, modificamos `application.properties` para cambiar la property `sec=sec09` y ejecutamos nuestra app `WebfluxPlaygroundApplication`.

Vamos al navegador, abrimos dos ventanas para simular dos usuarios diferentes, y vamos a la ruta `http://localhost:8080`.

En la primera ventana indicamos un max price `80` y en la segunda `50` y en ambas ventanas pulsamos el botón `Notify me!!`.

Veremos como van apareciendo los productos que cumplen ese filtro.

Pulsar el botón `Stop` cuando queramos dejar de recibir notificaciones.

## Integration Tests

Vamos a ver como escribir los tests de integración para la sección 09.

En `src/test/java/com/jmunoz/playground.tests.sec09` creamos la clase:

- `ServerSentEventsTest`: Tests de integración de SSE.

Para probarlo, solo tenemos que ejecutar este test.

# Performance Optimization

## Introduction

Aunque WebFlux es muy poderoso, hay ciertas tareas que no va a poder resolver.

En esta sección hablamos de configuraciones avanzadas y mejores prácticas que pueden ayudarnos a escalar nuestra aplicación.

- Antes de ejecutar otro servidor (escalamiento horizontal) o incrementar la RAM/CPU, gastando bastante dinero...
  - Si la app es lenta, ¿por qué?
  - ¿Cuál puede ser el problema?
  - ¡Como reducir costes de infraestructura!

Antes de continuar, tomamos las siguientes suposiciones:

- Un gran entendimiento de la programación reactiva.
  - Sin esto, no vamos a poder ni hacer debug de los problemas que tengamos.
- ¡No bloqueamos el event loop thread! Se entiende que comprendemos el porqué.

Todo esto viene del curso anterior `https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming`.

## gzip

En una arquitectura de microservicios, tendremos muchas aplicaciones hablando la una con la otra. Todas estas aplicaciones pueden ser parte de una red ejecutándose en un centro de datos diferente.

Nuestra aplicación puede llamar a un servicio proveedor de terceros como AWS, GCP, Shopify, etc.

![alt gZip](./images/42-gzip.png)

El cliente envía una petición al servidor, y este devuelve la respuesta al cliente, usando la red.

Muchas veces podemos notar fácilmente que la red está congestionada, pero es muy difícil demostrarlo. Normalmente, si el tiempo de respuesta es largo, tomará bastante tiempo al cliente recibir la respuesta. A más tamaño de respuesta, más tiempo tomará al cliente recibirla.

Por eso, a veces parece que alguna aplicación de servidor no funciona correctamente, su rendimiento es bajo. De ahí el pensamiento de que se necesitan más servidores.

- Si el tamaño de la respuesta es grande (en KBs), tomará más tiempo al cliente recibir la respuesta.
- El cliente podría observar un incremento en el tiempo de respuesta. Las apps pueden aparentar un rendimiento pobre.
- Pero puede que solo sea un problema de latencia de red.

Aquí es donde entra en juego `gZip`.

- Es una técnica que puede usar el servidor para comprimir la respuesta antes de enviarla por la red.

Como el tamaño se ha reducido debido a la compresión, ahora llegará antes al cliente, así que se mejora el tiempo de respuesta y el rendimiento.

Antes de habilitar `gzip` tenemos que tener en cuenta lo siguiente:

- ¡Funciona bien en una **red congestionada** donde el **tamaño de la respuesta es grande**!
- Notas:
  - El servidor requiere procesamiento adicional para realizar la compresión.
  - Puede tener efectos negativos cuando el tamaño de la respuesta es pequeño.
  - ¡NO USAR LA MÁQUINA LOCAL PARA TESTEARLO (no va a haber latencia de red) ! ¡NO VAS A VER NINGUNA MEJORA!
    - ¡Hay que desplegar el proyecto a un servidor y hacer pruebas con JMeter!

## Enabling gzip

Vamos a ver como habilitar la compresión `gzip`.

En la parte del servidor, en el fichero `application.properties`, usaremos las siguientes propiedades para habilitar gzip:

```
# Server Side:
server.compression.enabled=true
server.compression.min-response-size=2048
server.compression.mime-types=application/json,application/xml
```

En este ejemplo, habilitamos `gzip` para respuestas con tamaño de al menos `2Kb` y solo para `json, xml`.

En la parte del cliente, en el header de la petición tenemos que incluir:

```
# Client Side Request Header:
Accept-Encoding: gzip
```

Para que el cliente entienda la respuesta obtenida. Si no enviamos esta propiedad en la cabecera, el server no hará la compresión.

IMPORTANTE: Tenemos que medir el rendimiento, porque puede que no tenga impacto.

## Connection Pooling

- La configuración de la conexión toma tiempo.
- Keep-alive => para reutilizar conexiones.
- HTTP 1.1
  - 1 conexión por request

![alt Connection Pooling](./images/43-ConnectionPooling.png)

Imaginemos que tenemos nuestra aplicación de servidor escuchando en el puerto 8080 y nuestra aplicación cliente.

Cuando el cliente quiere enviar una petición al servidor, se requiere una `3 way handshake` para establecer la conexión. Esto consume tiempo.

Cuando tenemos que enviar muchas peticiones concurrentes al servidor remoto, tenemos que establecer conexiones una y otra vez, y esto afecta al rendimiento.

En estos casos podemos habilitar `Keep-alive` para reutilizar conexiones existentes. WebClient en varios casos lo hace automáticamente.

Cuando usamos `HTTP 1.1`, cosa muy común todavía, solo podemos usar una conexión por petición, es decir, una vez enviamos la petición, usando un puerto de salida aleatorio, hasta que la respuesta sea devuelta, no podemos enviar otra petición por esa conexión. Si que se puede establecer otra conexión distinta desde el cliente al servidor para mandar una nueva petición.

![alt HTTP 1.1 Connections](./images/44-http11Connections.png)

## Keep Alive / Connection Pooling - Project Setup

Vamos a jugar con `Connection Pooling` en esta clase.

Ejecutar `java -jar external-services.jar`.

Vamos a trabajar con el endpoint `/demo03/product/{id}`: `http://localhost:7070/webjars/swagger-ui/index.html#/demo03`.

Este endpoint provee el nombre de producto para un id de producto (desde 1 a 20.000). Es un servicio lento cuya respuesta lleva hasta 5 segundos.

Usando Swagger, indicamos el valor `id = 1` y ejecutamos.

Ahora, para el proyecto, en `src/test/java/com/jmunoz/playground.tests.sec10` creamos los packages y clases siguientes (copiados de `sec07` salvo los que se indiquen):

- `dto`
  - `Product`
- `AbstractWebClient`
- `Lec01HttpConnectionPoolingTest`: Nueva clase

Ejecutamos el test `concurrentRequests()`.

## HTTP Connections via netstat

Para monitorizar las conexiones TCP de red de nuestro `external-services`, vamos a usar este comando: `netstat -an| grep -w 127.0.0.1.7070`.

Para que se refresque el comando cada 2 segundos, usamos la utilidad `watch` de esta manera: `watch 'netstat -an| grep -w 127.0.0.1.7070'`

Por tanto, antes de ejecutar el test ejecutamos nuestro `external-services` y el comando `netstat` usando `watch`.

Al ejecutar el test con el valor de la variable `max = 1` vemos dos entradas, correspondientes a una conexión. El puerto 7070 corresponde al servicio externo y el puerto 54716 a mi app.

![alt Netstat Demo](./images/45-NetstatDemo.png)

Una vez termina el hilo principal (1 minuto), pasa a estado de espera:

![alt Netstat Demo 2](./images/46-NetstatDemo2.png)

Y cuando pasa otro ratillo, ese `TIME_WAIT` también desaparece. 

Al ejecutar el test con el valor de la variable `max = 3` hacemos tres peticiones concurrentes. Vemos seis entradas, correspondientes a tres conexiones.

![alt Netstat Demo 3](./images/47-NetstatDemo3.png)

Por último, al ejecutar el test con el valor de la variable `max = 10` hacemos diez peticiones concurrentes. Vemos veinte entradas, correspondientes a diez conexiones.

![alt Netstat Demo 4](./images/48-NetstatDemo4.png)

Estos ejemplos muestran como el sistema operativo establece conexiones por las que podemos enviar peticiones y recibir respuestas.

## Configuring Connections Pool Size

En la clase `Lec01HttpConnectionPoolingTest` creamos un nuevo test `concurrentRequests2()` y lo ejecutamos, sin olvidarnos de ejecutar primero nuestro servicio externo.

Vemos que nos lleva 5 segundos el obtener 250 respuestas a nuestras 250 peticiones.

Vemos que nos lleva 10 segundos el obtener 260 respuestas a nuestras 260 peticiones.

Esta diferencia se debe a que `flatMap` se ejecuta en paralelo (`parallel`) hasta 256, siendo este número el tamaño de su cola. Esto también lo vimos en el curso anterior.

Esto es configurable, pudiendo indicarle a `flatMap` que lance más peticiones concurrentes a la vez.

Pero tener en cuenta que hay otro límite, el de WebClient, que por defecto (es configurable también) solo puede manejar hasta 500 conexiones con un servicio remoto.

La configuración de WebClient es como sigue (createWebClient es un método nuestro que puede verse en `AbstractWebClient`):

```java
// Esta es la forma de configurar WebClient para permitir más conexiones que las por defecto (500).
private final WebClient client = createWebClient(b -> {
    // Creamos un provider
    // El nombre del builder da igual, he puesto jomuma.
    // Se recomienda usar LIFO en vez de FIFO para que intente cerrar las conexiones más antiguas.
    // pendingAcquireMaxCount -> Si el máximo es 501 peticiones, el resto (si quiero 1000) los ponemos en la cola.
    //      Como si fuera el tamaño de la cola.
    var poolSize = 501;
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
```

Con esta configuración, obtener 501 resultados de 501 peticiones nos lleva de nuevo 5 segundos.

## Pool Size Calculation

Que podamos ajustar el `poolSize` no significa que tengamos que hacerlo. Lo hemos hecho para fines de demostración.

En la vida real no nos va a hacer falta ajustar este tamaño del pool.

Lo que ocurre es que, en que nuestro ejemplo, el servicio remoto es muy lento (5 sg) y estamos enviando muchas peticiones concurrentes (500).

Pero en la vida real, nuestra aplicación podría llamar a un servicio que puede responder en, digamos, 100 ms. Es decir, que con 500 conexiones podemos procesar 5000 peticiones por segundo, que es muchísimo.

Por tanto, SOLO SI ES ABSOLUTAMENTE NECESARIO TENDREMOS QUE AJUSTAR EL `poolSize`.

## SocketException - Too Many Open Files Issue

En esta clase vamos a ver otro cuello de botella.

En la clase `Lec01HttpConnectionPoolingTest` creamos un nuevo test `concurrentRequests3()` con el que queremos procesar 10000 peticiones concurrentes, y lo ejecutamos.

Aunque al instructor le da un error `SocketException: Too many open files`, a mi me devuelve resultados, pero tras 6 sg.

Independientemente del resultado concreto para esta prueba, lo importante es lo siguiente: En un momento dado vamos a enfrentarnos a problemas de este tipo (excepciones de socket, reseteo de conexiones...)

Esto es porque no podemos configurar WebClient aumentando el máximo de conexiones posibles así como así. Nuestro Sistema Operativo es finito y en algún momento va a fallar, alcanzando ese cuello de botella, y no vamos a poder hacer nada porque no depende de WebFlux, sino del Sistema Operativo.

No olvidar que gracias a `gzip` obtendremos antes la respuesta y que esa conexión se podrá volver a usar.

## HTTP2 Introduction

- HTTP 1.1 => 1997
- HTTP2 => 2015

![alt HTTP 1.1 vs HTTP2](./images/49-HTTP1vsHTTP2.png)

Ya hemos visto como funciona HTTP 1.1. Fue estandarizado a finales de los 90 y casi todos seguimos usándolo en producción en nuestra arquitectura de microservicios.

Pero, como hemos visto, tiene limitaciones. Necesita una comunicación por petición (la primera imagen de arriba). Por tanto, para procesar `x` peticiones concurrentes necesita `x` conexiones.

Recordar que no hablamos de hilos, sino de conexiones.

Para evitar estos problemas, se propuso un nuevo protocolo HTTP2 que se estandarizó en 2015 (la segunda imagen de arriba).

HTTP2 no necesita tantas conexiones. De hecho, solo necesita una conexión por la que puede enviar muchas peticiones concurrentes. Esto hace que no necesite de tantos recursos del sistema.

Sin embargo, la adopción de HTTP2 se ha ido incrementando lentamente en estos últimos años.

**Características**

- Multiplexing: Es el nombre que se le da a La habilidad de enviar muchas peticiones concurrentes usando una sola conexión. 
- HTTP2 es un protocolo binario (HTTP 1.1 era un protocolo textual)
- HTTP2 habilita técnicas de compresión del header, para reducir el tamaño de las cabeceras de la petición y de la respuesta.
  - Esto lleva a tiempos de transmisión más rápidos.

En general, podemos transmitir mensajes de forma mucho más eficiente entre el cliente y el servidor usando HTTP2.

## HTTP2 - Demo

Vamos a jugar con HTTP2.

No olvidar ejecutar nuestro servicio externo: `java -jar external-services.jar`.

- Nuestro servicio externo `external-services.jar` soporta HTTP2.
  - El server soporta tanto HTTP 1.1 como HTTP2.
- El cliente debe enviar qué protocolo quiere usar.

Para habilitar HTTP2 en el lado del servidor, en `application.properties` tenemos que indicar la siguiente propiedad:

```
// server side
server.http2.enabled=true
```

Cuando habilitamos esta propiedad en el servidor, este soportará tanto HTTP 1.1 como HTTP2. Esto es por si el cliente no entiende HTTP2, que podamos seguir trabajando con HTTP 1.1, aunque hay formas de indicar que solo se soporte HTTP2. 

`external-services.jar` lo tiene habilitado.

En el proyecto `src/test/java/com/jmunoz/playground.tests.sec10` creamos la clase:

- `Lec02Http2Test`

Para monitorizar las conexiones TCP de red de nuestro `external-services`, vamos a usar este comando: `watch 'netstat -an| grep -w 127.0.0.1.7070'`

Ejecutamos el test con la variable `var max = 3` y vemos que usamos una sola conexión.

Ejecutamos el test con la variable `var max = 100` y vemos que usamos también una sola conexión.

Ejecutamos el test con la variable `var max = 10_000` y vemos que usamos también una sola conexión, y todas las peticiones se han procesado correctamente en apenas 6 segundos. Este segundo de más es debido al tiempo de CPU que requiere decodificar 10.000 mensajes.

**NOTAS**

- Podríamos no ver el beneficio de usar HTTP2 si no tenemos muuuchas peticiones concurrentes.
- Tenemos que asegurarnos que el `load balancer` soporte HTTP2.

## SubscribeOn - For Blocking IO

NOTA: Esta parte es repetida de lo ya dicho en la parte JPA en Spring WebFlux.

- ¡Tenemos que intentar usar librerías reactivas en nuestra aplicación!
  - R2DBC, mongo, redis, kafka, pulsar, elasticsearch...
- ¿Qué pasa si no las uso?

Si no usamos ninguna librería reactiva en nuestra aplicación, esta es la alternativa:

![alt Subscribe On](./images/50-SubscribeOn.png)

Notar que se usa el scheduler `boundedElastic`.

Si nuestra librería hace una petición IO y no es reactiva, probablemente sea una petición bloqueante (me refiero a invokeMethod()).

Para evitar bloquear el event loop thread, usaremos el operador `subscribeOn()` junto con el scheduler `boundedElastic`.

Si usamos Java 21 o superior, también podemos usar Virtual Threads.

## Summary

![alt Performance Optimization - Summary](./images/51-PerformanceOptimizationSummary.png)

Si ni con todo lo que hemos visto somos capaces de resolver nuestro problema, es posible que tengamos que usar otra herramienta/tecnología para hacer nuestra aplicación más escalable y resiliente.

Por ejemplo, Redis, un sistema de almacenaje superrápido diseñado para aplicaciones que necesitan acceso a la data en tiempo real. Simplifica tareas como caching y gestión de data.

Otra tecnología es GraphQL, usando para transferencia eficiente de data entre el frontend y el backend.

El uso de patrones de diseño puede hacer nuestras aplicaciones más resilientes y manejar transacciones distribuidas.