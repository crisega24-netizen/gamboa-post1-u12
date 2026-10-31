package com.empresa.reembolsos;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion del flujo completo: HTTP -> controlador -> servicio -> validacion (RF1)
 * -> calculo (RF2) -> base de datos H2 -> notificacion (RF3).
 *
 * <p>Cada prueba usa su propio empleado, porque la base en memoria se comparte entre pruebas y el
 * validador de duplicados la consulta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoSolicitudReembolsoIntegracionTest {

    @Autowired
    private MockMvc mvc;

    private static String json(String empleado, String categoria, String monto, int dias, boolean documentos) {
        return """
                {"empleadoId":"%s","categoria":"%s","montoSolicitado":%s,
                 "fechaGasto":"2026-10-15","diasViaje":%d,"documentosSoporte":%b}
                """.formatted(empleado, categoria, monto, dias, documentos);
    }

    /** Compara montos por valor numerico (800000 y 800000.0 son el mismo monto). */
    private static Matcher<Object> monto(String esperado) {
        return new CustomTypeSafeMatcher<>("monto igual a " + esperado) {
            @Override
            protected boolean matchesSafely(Object valor) {
                return new BigDecimal(valor.toString()).compareTo(new BigDecimal(esperado)) == 0;
            }
        };
    }

    private long crear(String empleado) throws Exception {
        var respuesta = mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(json(empleado, "CAPACITACION", "1000000", 0, true)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private void cambiarEstado(long id, String estado) throws Exception {
        mvc.perform(put("/api/solicitudes/{id}/estado", id).contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + estado + "\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(estado));
    }

    @ParameterizedTest(name = "{1} por {2} ({3} dias) -> aprobado {4}")
    @CsvSource({
            "E-101, VIAJE,          400000,  3, 400000",   // tope 3 x 150.000 = 450.000; con el tope fijo de la guia se rechazaria
            "E-102, REPRESENTACION, 1500000, 0, 1500000",
            "E-103, CAPACITACION,   1000000, 0, 800000"
    })
    void creaLaSolicitudYCalculaElMontoAprobadoSegunLaCategoria(String empleado, String categoria, String monto,
                                                                int dias, String aprobado) throws Exception {
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(json(empleado, categoria, monto, dias, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.montoAprobado").value(monto(aprobado)));
    }

    @Test
    void rechazaLaSolicitudSinDocumentosSoporteConUnMensajeClaro() throws Exception {
        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("E-201", "REPRESENTACION", "500000", 0, false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud de reembolso rechazada"))
                .andExpect(jsonPath("$.detail").value("Faltan documentos soporte"));
    }

    @Test
    void rechazaUnaSegundaSolicitudPendienteDeLaMismaCategoriaEnElPeriodo() throws Exception {
        crear("E-202");

        mvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("E-202", "CAPACITACION", "200000", 0, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Ya existe una solicitud pendiente de la misma categoria en el periodo"));
    }

    @Test
    void alAprobarseNotificaALosTresObservadores(CapturedOutput salida) throws Exception {
        long id = crear("E-301");

        cambiarEstado(id, "APROBADA");

        assertThat(salida)
                .contains("Notificando a E-301: su solicitud " + id + " paso de PENDIENTE a APROBADA")
                .contains("Contabilidad: programar pago de 800000 para E-301 (solicitud " + id + ")")
                .contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a APROBADA");
    }

    @Test
    void alRechazarseContabilidadNoActua(CapturedOutput salida) throws Exception {
        long id = crear("E-302");
        int antes = salida.getAll().length();

        cambiarEstado(id, "RECHAZADA");

        var nueva = salida.getAll().substring(antes);
        assertThat(nueva)
                .contains("Notificando a E-302: su solicitud " + id + " paso de PENDIENTE a RECHAZADA")
                .contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a RECHAZADA")
                .doesNotContain("Contabilidad");
    }

    @Test
    void respondeNotFoundSiLaSolicitudNoExiste() throws Exception {
        mvc.perform(put("/api/solicitudes/{id}/estado", 9999).contentType(MediaType.APPLICATION_JSON)
                        .content("\"APROBADA\""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Solicitud no encontrada: 9999"));
    }
}
