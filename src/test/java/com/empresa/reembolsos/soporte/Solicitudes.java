package com.empresa.reembolsos.soporte;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fabrica de solicitudes para las pruebas (evita repetir los setters en cada caso). */
public final class Solicitudes {

    private Solicitudes() {
    }

    public static SolicitudReembolso solicitud(String empleado, CategoriaGasto categoria, String monto,
                                               LocalDate fecha, int diasViaje, boolean documentos) {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setEmpleadoId(empleado);
        s.setCategoria(categoria);
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setFechaGasto(fecha);
        s.setDiasViaje(diasViaje);
        s.setDocumentosSoporte(documentos);
        s.setEstado(EstadoSolicitud.PENDIENTE);
        return s;
    }

    public static SolicitudReembolso conMonto(CategoriaGasto categoria, String monto, int diasViaje) {
        return solicitud("E-001", categoria, monto, LocalDate.of(2026, 10, 15), diasViaje, true);
    }
}
