package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.dominio.CategoriaGasto.REPRESENTACION;
import static com.empresa.reembolsos.soporte.Solicitudes.conMonto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "1500000, 1500000",   // monto completo
            "2000000, 2000000",   // exactamente en el tope mensual
            "2500000, 2000000"    // sobre el tope: hasta 2.000.000
    })
    void reembolsaElMontoCompletoHastaElTopeMensual(String monto, String esperado) {
        assertEquals(0, new BigDecimal(esperado).compareTo(calculadora.calcular(conMonto(REPRESENTACION, monto, 0))));
    }
}
