package com.empresa.reembolsos.infraestructura;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistencia de las solicitudes con Spring Data JPA. El servicio de aplicacion depende de esta
 * interfaz directamente, sin un puerto intermedio (capas simples; ver ADR-001 en la Parte 2).
 */
public interface RepositorioSolicitudesJpa extends JpaRepository<SolicitudReembolso, Long> {

    List<SolicitudReembolso> findByEmpleadoIdAndEstado(String empleadoId, EstadoSolicitud estado);
}
