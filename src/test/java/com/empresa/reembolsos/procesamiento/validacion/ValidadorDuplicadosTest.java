package com.empresa.reembolsos.procesamiento.validacion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.dominio.CategoriaGasto.CAPACITACION;
import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static com.empresa.reembolsos.soporte.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();
    private final LocalDate octubre2026 = LocalDate.of(2026, 10, 15);

    @Test
    void rechazaOtraPendienteDeLaMismaCategoriaEnElMismoMes() {
        var pendiente = solicitud("E-001", REPRESENTACION, "300000", LocalDate.of(2026, 10, 2), 0, true);

        var resultado = validador.validar(
                solicitud("E-001", REPRESENTACION, "500000", octubre2026, 0, true), List.of(pendiente));

        assertFalse(resultado.valida());
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo", resultado.motivoRechazo());
    }

    @Test
    void apruebaElMismoMesDeOtroAnio() {
        // Comparando solo el mes (getMonth), octubre de 2025 se tomaria como duplicado de octubre de 2026.
        var pendiente = solicitud("E-001", REPRESENTACION, "300000", LocalDate.of(2025, 10, 15), 0, true);

        assertTrue(validador.validar(
                solicitud("E-001", REPRESENTACION, "500000", octubre2026, 0, true), List.of(pendiente)).valida());
    }

    @Test
    void apruebaOtraCategoriaEnElMismoMes() {
        var pendiente = solicitud("E-001", CAPACITACION, "300000", octubre2026, 0, true);

        assertTrue(validador.validar(
                solicitud("E-001", REPRESENTACION, "500000", octubre2026, 0, true), List.of(pendiente)).valida());
    }

    @Test
    void apruebaCuandoElEmpleadoNoTienePendientes() {
        assertTrue(validador.validar(
                solicitud("E-001", REPRESENTACION, "500000", octubre2026, 0, true), List.of()).valida());
    }
}
