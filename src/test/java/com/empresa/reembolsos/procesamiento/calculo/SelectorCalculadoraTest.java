package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectorCalculadoraTest {

    @Test
    void entregaLaEstrategiaRegistradaParaCadaCategoria() {
        var selector = new SelectorCalculadora(List.of(
                new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()));

        assertInstanceOf(CalculadoraViaje.class, selector.obtener(CategoriaGasto.VIAJE));
        assertInstanceOf(CalculadoraRepresentacion.class, selector.obtener(CategoriaGasto.REPRESENTACION));
        assertInstanceOf(CalculadoraCapacitacion.class, selector.obtener(CategoriaGasto.CAPACITACION));
    }

    @Test
    void rechazaUnaCategoriaSinEstrategiaRegistrada() {
        var selector = new SelectorCalculadora(List.of(new CalculadoraViaje()));

        var error = assertThrows(IllegalArgumentException.class,
                () -> selector.obtener(CategoriaGasto.CAPACITACION));
        assertEquals("Categoria no soportada: CAPACITACION", error.getMessage());
    }
}
