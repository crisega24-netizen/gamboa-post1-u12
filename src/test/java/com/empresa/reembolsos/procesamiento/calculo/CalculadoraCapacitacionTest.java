package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.dominio.CategoriaGasto.CAPACITACION;
import static com.empresa.reembolsos.soporte.Solicitudes.conMonto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "1000000, 800000",     // 80 % de 1.000.000
            "3000000, 2400000",    // 80 % del maximo solicitable
            "4000000, 3000000"     // 80 % = 3.200.000, se limita al tope anual de 3.000.000
    })
    void reembolsaElOchentaPorCientoHastaElTopeAnual(String monto, String esperado) {
        assertEquals(0, new BigDecimal(esperado).compareTo(calculadora.calcular(conMonto(CAPACITACION, monto, 0))));
    }
}
