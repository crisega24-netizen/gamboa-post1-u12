package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.empresa.reembolsos.dominio.CategoriaGasto.VIAJE;
import static com.empresa.reembolsos.soporte.Solicitudes.conMonto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @ParameterizedTest(name = "{0} en {1} dias -> {2}")
    @CsvSource({
            "400000, 3, 400000",   // bajo el tope (3 x 150.000 = 450.000): se reembolsa completo
            "450000, 3, 450000",   // exactamente en el tope
            "500000, 3, 450000"    // sobre el tope: se reembolsa hasta 450.000
    })
    void reembolsaHastaElTopeDiarioPorLosDiasDelViaje(String monto, int dias, String esperado) {
        assertEquals(0, new BigDecimal(esperado).compareTo(calculadora.calcular(conMonto(VIAJE, monto, dias))));
    }
}
