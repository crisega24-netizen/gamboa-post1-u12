package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rechaza las solicitudes cuyo monto supera el tope de su categoria.
 *
 * <p>El tope no se define aqui: se le pregunta a la estrategia de calculo de la categoria
 * ({@link com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso#topeSolicitud}).
 * Asi el tope de viaje es 150.000 por dia (y no 150.000 en total), los topes existen en un
 * solo lugar y una categoria nueva no obliga a modificar este validador.
 */
@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {

    private final SelectorCalculadora calculadoras;

    public ValidadorMontoMaximo(SelectorCalculadora calculadoras) {
        this.calculadoras = calculadoras;
    }

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        var tope = calculadoras.obtener(solicitud.getCategoria()).topeSolicitud(solicitud);
        return solicitud.getMontoSolicitado().compareTo(tope) > 0
                ? ResultadoValidacion.rechazar(
                        "Monto excede el tope de la categoria " + solicitud.getCategoria() + " (" + tope.toPlainString() + ")")
                : ResultadoValidacion.ok();
    }
}
