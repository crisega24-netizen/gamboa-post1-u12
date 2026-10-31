package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado para una categoria de gasto (patron Strategy, RF2).
 *
 * <p>Cada categoria encapsula en una sola clase todo lo que la distingue: su formula de
 * reembolso y el tope maximo que puede solicitarse. Agregar una categoria nueva consiste en
 * crear otra implementacion anotada con {@code @Component}; ninguna clase existente cambia.
 */
public interface CalculadoraReembolso {

    /** Categoria de gasto que atiende esta estrategia; clave con la que la registra el selector. */
    CategoriaGasto getCategoria();

    /**
     * Monto maximo que puede solicitarse en esta categoria. Lo consulta la validacion de RF1
     * para no duplicar los topes fuera de la estrategia que los define.
     */
    BigDecimal topeSolicitud(SolicitudReembolso solicitud);

    /** Monto aprobado segun la formula propia de la categoria. */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
