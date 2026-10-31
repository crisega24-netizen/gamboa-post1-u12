package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud de reembolso (patron Observer, RF3).
 *
 * <p>El servicio de aplicacion recibe de Spring todas las implementaciones y, tras persistir un
 * cambio de estado, avisa a cada una sin conocer que hace con el aviso. Cada interesado decide
 * por si mismo si el cambio le importa (Contabilidad, por ejemplo, solo reacciona a APROBADA).
 * Un interesado nuevo, como el panel de metricas, se agrega con otra implementacion anotada con
 * {@code @Component}, sin modificar el servicio ni los observadores existentes.
 */
public interface ObservadorCambioEstado {

    /**
     * @param solicitud      solicitud ya guardada, con su estado nuevo
     * @param estadoAnterior estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
