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
class NotificadorEmpleadoTest {

    private final NotificadorEmpleado notificador = new NotificadorEmpleado();

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = {"APROBADA", "RECHAZADA", "REQUIERE_CORRECCION"})
    void avisaAlEmpleadoDeCualquierResultado(EstadoSolicitud nuevoEstado, CapturedOutput salida) {
        notificador.onCambioEstado(solicitudEn(nuevoEstado), PENDIENTE);

        assertThat(salida).contains("Notificando a E-001: su solicitud 7 paso de PENDIENTE a " + nuevoEstado);
    }
}
