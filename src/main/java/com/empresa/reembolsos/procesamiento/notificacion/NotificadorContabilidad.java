package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Pide a Contabilidad programar el pago, solo cuando la solicitud queda APROBADA. */
@Component
public class NotificadorContabilidad implements ObservadorCambioEstado {

    private static final Logger log = LoggerFactory.getLogger(NotificadorContabilidad.class);

    @Override
    public void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior) {
        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            return;
        }
        log.info("Contabilidad: programar pago de {} para {} (solicitud {})",
                solicitud.getMontoAprobado().stripTrailingZeros().toPlainString(), solicitud.getEmpleadoId(), solicitud.getId());
    }
}
