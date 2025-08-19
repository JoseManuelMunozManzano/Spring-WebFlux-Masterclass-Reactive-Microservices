package com.jmunoz.playground.sec04.advice;

import com.jmunoz.playground.sec04.exceptions.CustomerNotFoundException;
import com.jmunoz.playground.sec04.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

// En caso de cualquier señal de error, se dispara este méto-do, basado en la clase de excepción.
@ControllerAdvice
public class ApplicationExceptionHandler {

    // Cuando se haga un throw de la excepción CustomerNotFound, se invoca este méto-do.
    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleException(CustomerNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        // No tenemos URL, pero creamos type para dar una idea.
        // Aquí estaríamos diciendo a nuestro caller que lea sobre el problema que le hemos devuelto.
        // No es obligatorio.
        problem.setType(URI.create("http://example.com/problems/customer-not-found"));
        problem.setTitle("Customer Not Found");
        return problem;
    }

    @ExceptionHandler(InvalidInputException.class)
    public ProblemDetail handleException(InvalidInputException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("http://example.com/problems/invalid-input"));
        problem.setTitle("Invalid Input");
        return problem;
    }
}
