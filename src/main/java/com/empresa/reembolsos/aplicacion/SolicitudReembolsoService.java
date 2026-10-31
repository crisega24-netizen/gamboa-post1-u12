package com.empresa.reembolsos.aplicacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.empresa.reembolsos.procesamiento.validacion.CadenaValidadores;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aplicacion: el unico punto que coordina los tres patrones y el unico que conoce
 * el repositorio de persistencia.
 *
 * <ul>
 *   <li>RF1 - valida la solicitud con la cadena de validadores (Chain of Responsibility).</li>
 *   <li>RF2 - calcula el monto aprobado con la estrategia de su categoria (Strategy).</li>
 *   <li>RF3 - avisa a todos los interesados cuando cambia el estado (Observer).</li>
 * </ul>
 */
@Service
public class SolicitudReembolsoService {

    private final CadenaValidadores validadores;
    private final SelectorCalculadora calculadoras;
    private final List<ObservadorCambioEstado> observadores;
    private final RepositorioSolicitudesJpa repositorio;

    public SolicitudReembolsoService(CadenaValidadores validadores, SelectorCalculadora calculadoras,
                                     List<ObservadorCambioEstado> observadores,
                                     RepositorioSolicitudesJpa repositorio) {
        this.validadores = validadores;
        this.calculadoras = calculadoras;
        this.observadores = observadores;
        this.repositorio = repositorio;
    }

    public SolicitudReembolso crear(SolicitudReembolso solicitud) {
        var pendientes = repositorio.findByEmpleadoIdAndEstado(solicitud.getEmpleadoId(), EstadoSolicitud.PENDIENTE);
        var resultado = validadores.validar(solicitud, pendientes);
        if (!resultado.valida()) {
            throw new SolicitudInvalidaException(resultado.motivoRechazo());
        }
        solicitud.setMontoAprobado(calculadoras.obtener(solicitud.getCategoria()).calcular(solicitud));
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return repositorio.save(solicitud);
    }

    /**
     * Cambia el estado y, una vez guardado el cambio, notifica a cada observador. Se avisa despues
     * de {@code save()} para que ningun interesado reaccione a un cambio que no quedo persistido.
     */
    public SolicitudReembolso cambiarEstado(Long id, EstadoSolicitud nuevoEstado) {
        var solicitud = repositorio.findById(id).orElseThrow(() -> new SolicitudNoEncontradaException(id));
        var estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);
        var guardada = repositorio.save(solicitud);
        observadores.forEach(o -> o.onCambioEstado(guardada, estadoAnterior));
        return guardada;
    }
}
