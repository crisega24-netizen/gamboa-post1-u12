package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;

/**
 * Eslabon de la cadena de validacion de solicitudes (patron Chain of Responsibility, RF1).
 *
 * <p>Cada eslabon aplica una sola verificacion: si la solicitud la incumple, la cadena se corta
 * con el motivo del rechazo; si la cumple, la solicitud pasa al siguiente eslabon. Ningun
 * eslabon conoce cuantos eslabones hay despues de el, asi que una verificacion nueva (por
 * ejemplo, el limite anual acumulado) se agrega como otra subclase sin modificar las existentes.
 */
public abstract class ValidadorSolicitud {

    private ValidadorSolicitud siguiente;

    /** Enlaza el siguiente eslabon y lo devuelve, para encadenar en una sola expresion. */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /** Valida con este eslabon y, si aprueba, delega en el siguiente. Corta al primer rechazo. */
    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /** Verificacion propia del eslabon. */
    protected abstract ResultadoValidacion validarPropio(
            SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
