package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraRepresentacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraViaje;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static com.empresa.reembolsos.soporte.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CadenaValidadoresTest {

    /** Cuenta las veces que se ejecuta el ultimo eslabon, para comprobar el corte de la cadena. */
    static class DuplicadosContador extends ValidadorDuplicados {
        int ejecuciones;

        @Override
        protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
            ejecuciones++;
            return super.validarPropio(solicitud, pendientes);
        }
    }

    private final DuplicadosContador duplicados = new DuplicadosContador();
    private final CadenaValidadores cadena = new CadenaValidadores(
            new ValidadorDocumentosSoporte(),
            new ValidadorMontoMaximo(new SelectorCalculadora(List.of(
                    new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()))),
            duplicados);
    private final LocalDate fecha = LocalDate.of(2026, 10, 15);

    @Test
    void apruebaLaSolicitudQueSuperaLosTresEslabones() {
        var resultado = cadena.validar(solicitud("E-001", REPRESENTACION, "500000", fecha, 0, true), List.of());

        assertTrue(resultado.valida());
        assertEquals(1, duplicados.ejecuciones);
    }

    @Test
    void cortaEnElPrimerRechazoSinLlegarAlEslabonMasCostoso() {
        // Incumple las tres reglas: sin documentos, sobre el tope y duplicada. Se informa solo la primera.
        var pendiente = solicitud("E-001", REPRESENTACION, "100000", fecha, 0, true);

        var resultado = cadena.validar(
                solicitud("E-001", REPRESENTACION, "2500000", fecha, 0, false), List.of(pendiente));

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
        assertEquals(0, duplicados.ejecuciones);
    }

    @Test
    void elMontoSeEvaluaAntesQueLosDuplicados() {
        var pendiente = solicitud("E-001", REPRESENTACION, "100000", fecha, 0, true);

        var resultado = cadena.validar(
                solicitud("E-001", REPRESENTACION, "2500000", fecha, 0, true), List.of(pendiente));

        assertEquals("Monto excede el tope de la categoria REPRESENTACION (2000000)", resultado.motivoRechazo());
        assertEquals(0, duplicados.ejecuciones);
    }
}
