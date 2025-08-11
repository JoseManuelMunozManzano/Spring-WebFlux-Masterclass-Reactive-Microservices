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