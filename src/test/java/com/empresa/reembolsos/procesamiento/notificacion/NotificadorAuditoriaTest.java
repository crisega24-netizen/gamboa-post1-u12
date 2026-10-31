package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static com.empresa.reembolsos.dominio.EstadoSolicitud.PENDIENTE;
import static com.empresa.reembolsos.procesamiento.notificacion.CambiosDeEstado.solicitudEn;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class NotificadorAuditoriaTest {

    private final NotificadorAuditoria notificador = new NotificadorAuditoria();

    @ParameterizedTest
    @EnumSource(EstadoSolicitud.class)
    void registraTodosLosCambiosSinExcepcion(EstadoSolicitud nuevoEstado, CapturedOutput salida) {
        notificador.onCambioEstado(solicitudEn(nuevoEstado), PENDIENTE);

        assertThat(salida).contains("Auditoria: solicitud 7 cambio de PENDIENTE a " + nuevoEstado);
    }
}
