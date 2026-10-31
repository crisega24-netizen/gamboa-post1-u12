package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

import static com.empresa.reembolsos.soporte.Solicitudes.conMonto;

/** Solicitud guardada (id 7) que acaba de pasar al estado indicado. */
final class CambiosDeEstado {

    private CambiosDeEstado() {
    }

    static SolicitudReembolso solicitudEn(EstadoSolicitud nuevoEstado) {
        SolicitudReembolso s = conMonto(CategoriaGasto.CAPACITACION, "1000000", 0);
        s.setId(7L);
        s.setMontoAprobado(new BigDecimal("800000"));
        s.setEstado(nuevoEstado);
        return s;
    }
}
