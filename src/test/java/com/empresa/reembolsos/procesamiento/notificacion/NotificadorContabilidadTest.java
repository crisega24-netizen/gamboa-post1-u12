package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static com.empresa.reembolsos.dominio.EstadoSolicitud.APROBADA;
import static com.empresa.reembolsos.dominio.EstadoSolicitud.PENDIENTE;
import static com.empresa.reembolsos.procesamiento.notificacion.CambiosDeEstado.solicitudEn;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class NotificadorContabilidadTest {

    private final NotificadorContabilidad notificador = new NotificadorContabilidad();

    @Test
    void programaElPagoCuandoLaSolicitudQuedaAprobada(CapturedOutput salida) {
        notificador.onCambioEstado(solicitudEn(APROBADA), PENDIENTE);

        assertThat(salida).contains("Contabilidad: programar pago de 800000 para E-001 (solicitud 7)");
    }

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, mode = EnumSource.Mode.EXCLUDE, names = "APROBADA")
    void ignoraLosDemasEstados(EstadoSolicitud nuevoEstado, CapturedOutput salida) {
        int antes = salida.getAll().length(); // la captura acumula la salida de las pruebas anteriores de la clase

        notificador.onCambioEstado(solicitudEn(nuevoEstado), PENDIENTE);

        assertThat(salida.getAll().substring(antes)).doesNotContain("Contabilidad");
    }
}
