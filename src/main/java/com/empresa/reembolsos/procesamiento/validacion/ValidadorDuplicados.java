package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/**
 * Rechaza la solicitud si el empleado ya tiene otra pendiente de la misma categoria en el mismo
 * periodo (mes y anio).
 *
 * <p>No consulta la base de datos: recibe las solicitudes pendientes del empleado ya cargadas por
 * el servicio de aplicacion, de modo que este paquete no depende de la infraestructura.
 */
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    // VIOLACION INTENCIONAL (Paso 5 de la Parte 2): "atajo" para consultar la base de datos
    // directamente desde el validador. Compila y no rompe ninguna prueba funcional, pero
    // procesamiento pasa a depender de infraestructura: la Regla 1 de ArchUnit debe detectarlo.
    @Autowired
    private RepositorioSolicitudesJpa repositorio;

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        var periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientes.stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
                : ResultadoValidacion.ok();
    }
}
