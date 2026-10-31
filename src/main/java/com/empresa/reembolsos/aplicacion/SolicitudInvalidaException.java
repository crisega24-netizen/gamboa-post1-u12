package com.empresa.reembolsos.aplicacion;

/** La solicitud no supero la cadena de validacion; el mensaje es el motivo del rechazo. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String motivo) {
        super(motivo);
    }
}
