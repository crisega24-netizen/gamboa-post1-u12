package com.empresa.reembolsos.adaptadores.rest;

import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;
import com.empresa.reembolsos.aplicacion.SolicitudNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de la aplicacion a respuestas HTTP con formato Problem Details (RFC 9457):
 * una solicitud rechazada responde 400 con el motivo, en lugar de un 500 generico.
 */
@RestControllerAdvice
public class ManejadorErroresRest {

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ProblemDetail solicitudInvalida(SolicitudInvalidaException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problema.setTitle("Solicitud de reembolso rechazada");
        return problema;
    }

    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ProblemDetail solicitudNoEncontrada(SolicitudNoEncontradaException e) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Solicitud no encontrada");
        return problema;
    }
}
