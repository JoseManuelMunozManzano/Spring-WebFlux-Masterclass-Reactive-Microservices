# SPRING WEBFLUX MASTERCLASS: REACTIVE MICROSERVICES

Del curso de UDEMY: `https://www.udemy.com/course/spring-webflux/`

## Introducción

Esto es lo que veremos en el curso:

![alt Topics](./images/01-Topics.png)

Ya tenemos que saber programación reactiva, que incluye:

![alt Reactive Programming](./images/02-ReactiveProgramming.png)

Para esto, ver mi repositorio: `https://github.com/JoseManuelMunozManzano/Mastering-Java-Reactive-Programming/tree/main`.

En esta sección (y para las siguientes) nuestro objetivo es configurar un proyecto de prácticas para aprender `Spring WebFlux`.

Aprenderemos varias características de `WebFlux`, `R2DBC` (Reactive Data Access) y escribiremos tests de integración.

Al final, configuraremos otro proyecto para desarrollar un sistema reactivo.

## Traditional vs Reactive APIs

[README.md](./01-webflux-playground/README.md#traditional-vs-reactive-apis)

Para ver como se diferencia la programación de una API tradicional de una reactiva, ver proyecto `01-webflux-playground`, paquete `sec01` donde están las siguientes clases:

- `Product`
  - Es un record.
- `TraditionalWebController`
  - Es un controller hecho en programación tradicional.
- `ReactiveWebController`
  - Es un controller hecho en programación reactiva.

## Spring Data R2DBC

`R2DBC` significa `Reactive Relational Database Connectivity`.

Documentación:

- `https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/`.
- `https://r2dbc.io/drivers/`.
- `https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/#datatypes`.
- `https://docs.spring.io/spring-data/relational/reference/r2dbc.html`.
- `https://docs.spring.io/spring-data/relational/reference/r2dbc/query-methods.html`.
- `https://medium.com/@padiahrohit/enable-h2-console-in-java-reactive-environmant-dcfcfdd6858a`.

[README.md](./01-webflux-playground/README.md#spring-data-r2dbc)

Ver proyecto `01-webflux-playground`, paquete `sec02` para el proyecto con R2DBC.

- `WebfluxPlaygroundApplication.java`
    - El main, donde indicaremos que paquete tiene que escanear Spring para crear sus beans.
- `application.properties`
    - Indicamos la configuración de la BD para R2DBC.
- En `src/java/com/jmunoz/playground/sec02` creamos las clases:
  - `Customer`
    - Es la clase entity que representa a la tabla `customer`.
    - En el package entity.
  - `Product`
    - Es la clase entity que representa a la tabla `product`.
    - En el package entity.
  - `CustomerOrder`
    - Es la clase entity que representa a la tabla `customer_order`.
    - En el package entity.
  - `CustomerRepository`
    - Es la interfaz repository que usaremos para tener ya creado un CRUD.
    - Extiende de `ReactiveCrudRepository`.
    - En el package repository.
  - `ProductRepository`
    - Es la interfaz repository que usaremos para tener ya creado un CRUD.
    - Extiende de `ReactiveCrudRepository`.
    - En el package repository.
  - `CustomerOrderRepository`
    - Es la interfaz repository que usaremos para tener ya creado un CRUD.
    - Extiende de `ReactiveCrudRepository`.
    - En el package repository.
  - `OrderDetails`
    - Record con la representación de la fila que queremos obtener en la query.
    - En el package dto. 
- En `src/test/java/com/jmunoz/playground.tests.sec02` creamos la clase:
  - `AbstractTest`
    - Es una clase abstracta que extenderemos para hacer tests.
  - `Lec01CustomerRepositoryTest`
    - Es un test sobre la interface CustomerRepository.
  - `Lec02ProductRepositoryTest`
    - Es un test sobre la interface ProductRepository.
  - `Lec03CustomerOrderRepositoryTest`
    - Es un test sobre la interface CustomerOrderRepository.
  - `Lec04DatabaseClientTest`
    - Es un test para probar SQLs complejos usando DatabaseClient.

## R2DBC vs JPA/JDBC

Ver el proyecto `04-r2dbc-vs-jdbc` que da el profesor. Todo el proyecto ya viene codificado y solo tenemos que mirar las clases.

[README.md](./04-r2dbc-vs-jdbc/reactive-vs-traditional-postgres/README.md)

Es otra comparación entre reactivo y tradicional.

En concreto, se van a comparar las siguientes características del módulo Spring Data R2DBC contra el módulo Spring Data JPA:

- Eficiencia de recursos
  - Cuantos recursos de sistema usan.
- Rendimiento
  - Número de tareas ejecutadas por unidad de tiempo.

**Notas**

- No traeremos otras aplicaciones / capas web en el test, porque el resultado no sería preciso.
- No usaremos queries de larga ejecución.
  - Queremos testear r2dbc vs jdbc/jpa.
  - No el motor de BD.
  - Esas queries de larga ejecución serían buenas para comparar postgres contra mysql, cosa que no queremos.

**Configuración**

Esta es la configuración del proyecto:

![alt Configuración R2DBC vs JDBC](./images/03-R2dbcVsJDBCSetup.png)

- Ejecutar `docker-compose up`.
- Monitorizar la consola. Asegurar que tenemos 10 millones de customers insertados.
  - Esperar al mensaje `database system is ready to accept connections`.

## Reactive CRUD APIs with WebFlux

[README.md](./01-webflux-playground/README.md#reactive-crud-apis-with-webflux)

En esta sección vemos como exponer REST APIs con Spring WebFlux, en concreto los relacionados a Customer.

![alt Reactive Controller](./images/04-ReactiveController.png)

En secciones anteriores ya se crearon las clases de Entity y de Repository, así que podemos usarlas y, en esta sección, nos enfocamos en las clases service y controller.

Es decir, solo nos centraremos en los CRUD APIs, no vamos a gestionar excepciones ni validaciones de entrada. Se verán en secciones posteriores.

Ver proyecto `01-webflux-playground`, paquete `sec03`.

- `controller`
  - `CustomerController`
- `dto`
  - `CustomerDto`: Es un record.
- `entity`
  - `Customer`: Copiado de `sec02/entity`.
- `mapper`
  - `EntityDtoMapper`: Es sobre todo, una clase utility para convertir de entity a dto y al revés.
- `repository`
  - `CustomerRepository`: Copiado de `sec02/repository`.
- `service`
  - `CustomerService`

En `src/test/java/com/jmunoz/playground.tests.sec03` creamos la clase:

- `CustomerServiceTest`
  - Se hacen tests de integración. 

## Input Validation / Error Handling

[README.md](./01-webflux-playground/README.md#input-validation--error-handling)

En esta sección vamos a ver validaciones de entrada y gestión de errores.

Dada la aplicación realizada en el package `sec03` vamos a añadirle algunos requerimientos nuevos:

- CustomerDto debe venir informado name y email, este último con un formato válido.
- En vez de ResponseEntity, vamos a usar ControllerAdvice para manejar cualquier problema que queramos comunicarle al llamador.

Ver proyecto `01-webflux-playground`, paquete `sec04`. Los fuentes están copiados de `sec03`, salvo los que se indiquen.

- `controller`
    - `CustomerController`: Emitimos señal de error en vez de ResponseEntity si ocurre algún problema.
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
    - `ApplicationExceptionHandler`: En caso de cualquier señal de error, se dispara este método, basado en la clase de excepción. 
- `exceptions`: Nuevo package
    - `CustomerNotFoundException`
    - `InvalidInputException`
    - `ApplicationExceptions` :Actúa como un Exception Factory.
- `validator`: Nuevo package
    - `RequestValidator`: Tendremos aquí utility methods.

En `src/test/java/com/jmunoz/playground.tests.sec04` creamos la clase:

- `CustomerServiceTest`
    - Se hacen tests de integración. Los happy path siguen igual y se da la respuesta de los objetos Problem Detail en caso de problemas.

## WebFilter

[README.md](./01-webflux-playground/README.md#webfilter)

WebFilter es un componente intermediario entre el servidor y el controller, tiene la habilidad de manipular peticiones entrantes y respuestas salientes.

Ver proyecto `01-webflux-playground`, paquete `sec05`. Los fuentes están copiados de `sec04`, salvo los que se indiquen.

- `controller`
    - `CustomerController`: Accedemos al atributo establecido en el WebFilter.
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
    - `Category`: Es un enum con las categorías de llamadores permitidos.
    - `AuthenticationWebFilter`: Pasamos un atributo al siguiente WebFilter.
    - `AuthorizationWebFilter`: Trabajamos con el atributo pasado.
    - `FilterErrorHandler`: Solo si realmente necesitamos añadir `ProblemDetail` como parte de lo que devuelve nuestro `WebFilter`.

En `src/test/java/com/jmunoz/playground.tests.sec05` creamos la clase:

- `CustomerServiceTest`
    - Se hacen tests de integración de los WebFilter.

## Functional Endpoints

[README.md](./01-webflux-playground/README.md#functional-endpoints)

Tradicionalmente, exponemos APIs via controllers anotados como este, que funcionan perfectamente:

![alt Controller Annotation](./images/05-ControllerAnnotation.png)

En este caso, anotado con `@GetMapping` y `@PathVariable` que, de alguna manera, Spring invocará cuando hagamos una petición GET. Luego, tomamos el control y llamaremos a la capa de servicio.

Además de estos controller anotados, WebFlux provee una solución alternativa para exponer APIs llamada `functional endpoints`, que es lo que vamos a ver en esta sección.

![alt Functional Endpoints](./images/06-FunctionalEndpoints.png)

Ver proyecto `01-webflux-playground`, paquete `sec06`. Los fuentes están copiados de `sec04`, salvo los que se indiquen.

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
    - `ApplicationExceptionHanler`: Creamos Problem Detail y devolvemos Mono<ServerResponse> cuando hay excepción.
- `assignment`: Nuevo package
    - `CalculatorAssignment`: Es un ejercicio para jugar con Request Predicate.

En `src/test/java/com/jmunoz/playground.tests.sec06` creamos la clase:

- `CustomerServiceTest`
    - Se hacen tests de integración de los Functional Endpoints.
- `CalculatorTest`
    - Se hacen tests de integración de `CalculatorAssignment`.

## WebClient

[README.md](./01-webflux-playground/README.md#webclient)

Aunque ya usamos WebClient en la sección 1, ahora vamos a discutirla en detalle.

WebClient es sencillamente una plantilla REST Reactiva.

- API fluida basada en Reactor para hacer peticiones HTTP.
  - Envoltorio alrededor de reactor-netty
- Recibe la respuesta de manera No bloqueante
- Inmutable
- Thread safe

Ver proyecto `01-webflux-playground`, paquete `tests/sec07` para nuestras prácticas de WebClient.

- `dto`: Nuevo package
    - `Product`: Es un record.
    - `CalculatorResponse`: Es un record.
- `AbstractWebClient`
    - Clase abstracta que será extendida por varias clases. Aquí tendremos métodos de utilidad.
- `Lec01MonoTest`
    - Vamos a realizar una petición GET sencilla.
    - Vamos a trabajar con el endpoint `/demo02/lec01/product/{id}`
    - El id va de 1 a 100 y cada call le lleva 1 segundo responderlo. 
- `Lec02FluxTest`
    - Vamos a realizar una petición GET que devuelve una respuesta streaming.
    - Vamos a trabajar con el endpoint `/demo02/lec02/product/stream`
    - Este endpoint provee 10 productos como stream, con 500ms de delay entre cada uno de ellos. No es necesario pasar ningún id. 
- `Lec03PostTest`
    - Vamos a trabajar con el endpoint `/demo02/lec03/product`
    - Este endpoint acepta una petición POST para un producto y le lleva 1 segundo responder.
- `Lec04HeaderTest`
    - Vamos a trabajar con el endpoint `/demo02/lec04/product/{id}`
    - Este endpoint espera un header con la propiedad `caller-id`, es decir, estaríamos indicando quién llama al servicio. Si no viene este header, no enviaremos la respuesta.
- `Lec05ErrorResponseTest`
    - Vamos a trabajar con el endpoint `/demo02/lec05/calculator/{first}/{second}`
    - Hace la operación matemática y devuelve el resultado. Ambos parámetros deben ser mayores que cero. En el header mandamos `operación` y alguna de estas operaciones como valor `+, -, *, /`. En caso contrario la operación fallará con status 404 y devolverá un objeto ProblemDetail.
- `Lec06QueryParamsTest`
    - Vamos a trabajar con el endpoint `/demo02/lec06/calculator`
    - Es lo mismo que hemos visto en `Lec05ErrorResponseTest` pero en vez de usar `Path Variables` y un header, usamos `Query Parameters`.
- `Lec07BasicAuthTest`
    - Vamos a trabajar con el endpoint `/demo02/lec07/product/{id}`
    - El valor de id puede ir de 1 a 100.
    - Si no se envían las credenciales `username: java, password: secret` devuelve 401.
- `Lec08BearerAuthTest`
    - Vamos a trabajar con el endpoint `/demo02/lec08/product/{id}`
    - El valor de id puede ir de 1 a 100.
    - Si no se envía el bearer token `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9` devuelve 401.
- `Lec089ExchangeFilterTest`
    - Vamos a trabajar con el endpoint `/demo02/lec09/product/{id}`
    - El valor de id puede ir de 1 a 100.
    - Espera que se envíe un nuevo bearer token `Authorization: Bearer [generate new token]` cada vez.
    - Para enviar un nuevo token se usa `UUID.randomUUID().toString().replace("-", "")`.
    - También creamos otro Filter Function para añadir funcionalidad de logging.
    - Añadimos atributos a nuestra funcionalidad de logger. Nuestra clase de servicio va a enviar un flag para habilitar o no este logging.

## Streaming

[README.md](./01-webflux-playground/README.md#streaming)

Ver proyecto `01-webflux-playground`, paquete `sec08`.

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
    - `CustomerService`
- `service`
    - `ProductService`
- `controller`
    - `ProductController`: Expone Product Upload API y devuelve el Dto UploadResponse. Notar `consumes = MediaType.APPLICATION_NDJSON_VALUE` porque usamos ND JSON.

En `src/test/java/com/jmunoz/playground.tests`, paquete `sec08`.

- `ProductClient`: Invoca la API y envía los productos
- `ProductsUploadDownloadTest`: Para hacer la demo tanto de upload como de download (lo codificamos más adelante)
- `FileWriter`: Guardamos el millón de productos en fichero en la raiz del proyecto con nombre `products.txt`.

## Server Sent Events / SSE

Vamos a hablar de los Server Sent Events, también conocidos como SSE o EventSource.

[README.md](./01-webflux-playground/README.md#server-sent-events--sse)

Ver proyecto `01-webflux-playground`, paquete `sec09`. Los fuentes están copiados de `sec08`, salvo los que se indiquen.

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
    - `DataSetupService`: Añade un producto cada sg. Se ejecuta automáticamente al ejecutarse la app.
- `controller`
    - `ProductController`
- `config`: Nuevo package
    - `ApplicationConfig`: Exponemos un bean para Sinks donde emitimos `ProductDto`.

En `src/java/resources/static` creamos el siguiente archivo html.

- `index.html`: Es la UI de nuestra app del paquete `sec09`.

En `src/test/java/com/jmunoz/playground.tests.sec09` creamos la clase:

- `ServerSentEventsTest`: Tests de integración de SSE.

## Performance Optimization

Aunque WebFlux es muy poderoso, hay ciertas tareas que no va a poder resolver.

En esta sección hablamos de configuraciones avanzadas y mejores prácticas que pueden ayudarnos a escalar nuestra aplicación.

[README.md](./01-webflux-playground/README.md#performance-optimization)

Ver proyecto `01-webflux-playground`, paquete `tests/sec10` para nuestras prácticas de Connection Pooling.

Los fuentes están copiados de `sec07`, salvo los que se indiquen.

- `dto`
    - `Product`
- `AbstractWebClient`
- `Lec01HttpConnectionPoolingTest`: Nueva clase
- `Lec02Http2Test`: Nueva clase

## Final Project - Reactive Microservices

Ver los siguientes proyectos:

- `customer-service`
- `external-services.jar`: Es un servicio externo de terceros.
  - Ejecutar `java -jar external-services.jar`
  - Acceder a Swagger: `http://localhost:7070/webjars/swagger-ui/index.html#/demo04`
    - Los endpoints que nos interesan están bajo el apartado `demo04`.
- `aggregator-service`

### Trading Platform - Introduction

Vamos a desarrollar una plataforma de trading con estos servicios:

![alt Trading Platform](./images/07-TradingPlatform.png)

Tenemos tres servicios, uno de los cuales, `Aggregator-Service` es lo que se suele llamar un `servicio orquestador` o `BFF Backend For FrontEnd`, mientras que los otros dos servicios son servicios privados.

Primero vamos a ver el flujo de trabajo de alto nivel y luego bajaremos a los servicios individuales, detalles de la API de bajo nivel, etc.

**Workflow**

![alt Trading Platform Workflow](./images/08-TradingPlaformWorkflow.png)

`Stock-Service` emite cambios de precios de acciones periódicamente, como por ejemplo, acciones de Google a `$10`, Amazon `$8`, etc.

`Aggregator-Service` lo consumirá y lo expone como un `server send events API` para los clientes.

Los usuarios pueden observar los cambios de precios y decidir comprar/vender acciones cuando quieran.

Por ejemplo, veamos este flujo de trabajo.

Esto es lo que vamos a desarrollar, desde un punto de vista de alto nivel:

![alt Trading Platform Workflow 2](./images/09-TradingPlaformWorkflow2.png)

Imaginemos que queremos comprar 10 acciones de Google.

- El cliente envía la petición de comerciar (trade) a `Aggregator-Service`.
- `Aggregator-Service` llama a `Stock-Service` para obtener el precio actual de las acciones de Google antes de comprar/vender.
- `Aggregator-Service` llama a `Customer-Service` para finalizar la transacción de compra/venta.
- `Customer-Service` gestiona la información de cliente, portfolio, balance de cuentas, etc.
  - `BUY`: Si el cliente tiene saldo, se realizará la compra.
  - `SELL`: Si el cliente posee acciones, pueden venderse.
- El cliente podrá ver todas sus acciones/portfolio actual, cuando acceda a su perfil.

Vamos a ver ahora algunos de los detalles de bajo nivel que tenemos que tener en cuenta:

- `Stock-Service` es un servicio externo de terceros que no tenemos que implementar.
  - Lo tenemos que integrar en nuestro sistema.
- Como vamos a ejecutar varias aplicaciones de Spring Boot en local, los puertos que se van a emplear son los siguientes:
    - `Aggregator-Service`: 8080
    - `Customer-Service`: 6060
    - `Stock-Service`: Este servicio externo usa el puerto 7070

### Customer Portfolio - Requirements Discussion

En esta clase vamos a ver detalles de bajo nivel para `Customer-Service`.

![alt Customer Service Details](./images/10-CustomerServiceDetails.png)

- No vamos a preocuparnos por el típico CRUD como crear customer, delete customer, etc. Ya hemos hecho muchos en el curso.
- Nuestro foco va a consistir en gestionar el portfolio del customer.
- Viendo la imagen el significado es el siguiente:
  - Sam posee 10 acciones de GOOGLE.
  - Mike posee 2 acciones de APPLE.

El SQL que vamos a usar para este servicio es este:

```roomsql
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS portfolio_item;

CREATE TABLE customer (
    id int AUTO_INCREMENT primary key,
    name VARCHAR(50),
    balance int
);

CREATE TABLE portfolio_item (
    id int AUTO_INCREMENT primary key,
    customer_id int,
    ticker VARCHAR(10),
    quantity int,
    foreign key (customer_id) references customer(id)
);

insert into customer(name, balance)
    values
        ('Sam', 10000),
        ('Mike', 10000),
        ('John', 10000);
```

Hablemos de los detalles de la API:

![alt Customer Service API Details 1](./images/11-CustomerServiceAPIDetails1.png)

- Endpoint GET con un path variable `customerId`.
  - Devolveremos la información del record `CustomerInformation`.

![alt Customer Service API Details 2](./images/12-CustomerServiceAPIDetails2.png)

- Endpoint POST con un path variable `customerId`.
  - Recibiremos como body un objeto de tipo `StockTradeRequest`.
  - Devolveremos como respuesta un objeto de tipo `StockTradeResponse` si todo va bien.
    - `balance` indica la cantidad de saldo que queda una vez se ha realizado la orden.

Esto sería un ejemplo de ejecución:

![alt Customer Service Example](./images/13-CustomerServiceExample.png)

**BUY**

- Si cada acción de GOOGLE vale `$100` y quiero comprar 5 acciones, el precio total será de `$500`.
- Confirmo que Sam (el que quiere comprar la acciones), posee ese saldo. Como tiene `$2000` lo cumple.
  - Su saldo queda en `$1500`.
- Vamos a su portfolio y vemos que Sam ya posee acciones de GOOGLE.
  - Actualizamos la cantidad a 15 acciones.
- Si Sam no tuviera registros entonces se añade un nuevo registro con la cantidad de 5 acciones.

**SELL**

- Si cada acción de GOOGLE vale `$100` y quiero vender 5 acciones, el precio total será de `$500`.
- Confirmo que Sam (el que quiere vender la acciones), posee esa cantidad de acciones en su portfolio.
    - Como tiene 10, sus acciones se actualizan a 5.
    - Si tuviera 5 acciones, sus acciones se quedan a 0 (no se elimina el registro)
- Su saldo se incrementa, pasando a tener `$2500`.

**Exceptions**

Las posibles excepciones son estas:

- Customer Not Found
- Customer does NOT have enough balance
- Customer does NOT have enough shares
- Devolveremos el error como Problem Detail
  - 4xx error, donde xx será el número concreto del error

### Aggregator - Introduction

La misión de `AggregatorService` es de orquestación. Depende de `Stock-Service` y `Customer-Service`.

Hablemos de los detalles de la API:

![alt Aggregator Service API Details](./images/14-AggregatorServiceAPIDetails.png)

- Endpoint GET `/stock/price-stream`.
  - Obtendremos un stream de precios de acciones.
- Endpoint GET `/customers/{customerId}`
  - Lo llamaremos cuando el customer quiera obtener su portfolio.
- Endpoint POST `/customers/{customerId}/trade`
  - Lo llamaremos cuando el customer quiera comprar/vender acciones.
  - Tendremos que llamar al primer endpoint de `Stock-Service` para obtener el precio de esa acción.
  - Construiremos un objeto de tipo `StockTradeRequest` y haremos el POST a `Customer-Service`.

**Exceptions**

Las posibles excepciones son estas:

- Customer Not Found
  - Cuando `Customer-Service` devuelve 404
- Invalid Trade Request
  - Cuando fallan las validaciones de la petición de entrada
    - Estas son validaciones sencillas (no de dominio) que tendrá `Aggregator-Service`.
  - Cuando `Customer-Service` devuelve 400
    - Customer does NOT have enough balance
    - Customer does NOT have enough shares
- Devolveremos el error como Problem Detail

NOTA: `Stock-Service` no devuelve errores 4xx, por lo que no tenemos que preocuparnos de esto.

## Articles

Estos son algunos artículos interesantes que leer:

- Como podemos usar R2DBC para fines de audit logging: `https://www.vinsguru.com/r2dbc-entity-callback/`.
- Subida de ficheros usando Spring WebFlux: `https://www.vinsguru.com/spring-webflux-file-upload/`.
- Añadir Security con Spring WebFlux: `https://www.vinsguru.com/spring-webflux-security/`.