package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static com.empresa.reembolsos.soporte.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();
    private final LocalDate fecha = LocalDate.of(2026, 10, 15);

    @Test
    void apruebaLaSolicitudConDocumentosSoporte() {
        var resultado = validador.validar(solicitud("E-001", REPRESENTACION, "500000", fecha, 0, true), List.of());

        assertTrue(resultado.valida());
    }

    @Test
    void rechazaLaSolicitudSinDocumentosSoporte() {
        var resultado = validador.validar(solicitud("E-001", REPRESENTACION, "500000", fecha, 0, false), List.of());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }
}
