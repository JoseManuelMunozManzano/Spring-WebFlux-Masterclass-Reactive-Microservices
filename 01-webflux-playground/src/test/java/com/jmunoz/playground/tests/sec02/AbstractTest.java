package com.jmunoz.playground.tests.sec02;

import org.springframework.boot.test.context.SpringBootTest;

// Es una clase abstracta que extenderemos para crear tests.
// En la anotación indicamos, cuando se ejecuten los tests, que Spring tenga en cuenta
// la sec02.
// También se podría haber creado un application.properties para tests con esa property.
//
// Indicamos también la property logging.level.org.springframework.r2dbc=DEBUG para poder ver los SQLs que
// se están ejecutando.
// Esto también se puede indicar en application.properties.
@SpringBootTest(properties = {
        "sec=sec02",
//        "logging.level.org.springframework.r2dbc=DEBUG"
})
public abstract class AbstractTest {
}
