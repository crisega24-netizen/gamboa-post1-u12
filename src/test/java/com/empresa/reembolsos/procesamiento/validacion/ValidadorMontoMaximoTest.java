package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraRepresentacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraViaje;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static com.empresa.reembolsos.soporte.Solicitudes.conMonto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo(new SelectorCalculadora(List.of(
            new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion())));

    @ParameterizedTest(name = "{0} por {1} ({2} dias) -> valida={3}")
    @CsvSource({
            "VIAJE,          400000, 3, true",    // 3 dias x 150.000 = 450.000: con un tope fijo de 150.000 se rechazaria
            "VIAJE,          450000, 3, true",
            "VIAJE,          450001, 3, false",
            "REPRESENTACION, 2000000, 0, true",
            "REPRESENTACION, 2500000, 0, false",
            "CAPACITACION,   3000000, 0, true",
            "CAPACITACION,   3500000, 0, false"
    })
    void comparaElMontoContraElTopeDeLaEstrategiaDeLaCategoria(CategoriaGasto categoria, String monto,
                                                              int dias, boolean esperado) {
        assertEquals(esperado, validador.validar(conMonto(categoria, monto, dias), List.of()).valida());
    }

    @Test
    void elMotivoDelRechazoIndicaLaCategoriaYSuTope() {
        var resultado = validador.validar(conMonto(CategoriaGasto.VIAJE, "500000", 3), List.of());

        assertFalse(resultado.valida());
        assertEquals("Monto excede el tope de la categoria VIAJE (450000)", resultado.motivoRechazo());
    }
}
