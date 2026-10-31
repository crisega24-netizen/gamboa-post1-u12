package com.empresa.reembolsos;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Limites de arquitectura que si existen en el sistema, verificados en cada build.
 *
 * <p>Ninguna regla asume puertos de persistencia, coherente con ADR-001 (capas simples). Se
 * analizan solo las clases de produccion: las clases de prueba viven en los mismos paquetes
 * (por ejemplo {@code CalculadoraViajeTest} en {@code procesamiento.calculo}) y no deben
 * cumplir las reglas del codigo que prueban.
 */
@AnalyzeClasses(packages = "com.empresa.reembolsos", importOptions = ImportOption.DoNotIncludeTests.class)
public class ReglasArquitectura {

    /** Regla 1: los patrones no conocen la base de datos ni la capa web (Decision de diseno 4). */
    @ArchTest
    static final ArchRule procesamientoSinPersistenciaNiWeb = noClasses()
            .that().resideInAPackage("..procesamiento..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "..infraestructura..", "..adaptadores..",
                    "org.springframework.data..", "jakarta.persistence..",
                    "org.springframework.web..", "org.springframework.http..")
            .because("validar, calcular y notificar son reglas de negocio: los datos les llegan "
                    + "desde el servicio de aplicacion (ver ADR-001 y Decision de diseno 4)");

    /**
     * Regla 2: el controlador solo habla con el servicio de aplicacion y el dominio.
     *
     * <p>Refinada respecto a la guia: se permite el propio paquete {@code ..adaptadores.rest..},
     * porque ArchUnit cuenta como "acceso" leer el campo {@code servicio} del propio controlador.
     */
    @ArchTest
    static final ArchRule controladorSoloUsaServicioYDominio = classes()
            .that().resideInAPackage("..adaptadores.rest..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                    "..adaptadores.rest..", "..aplicacion..", "..dominio..",
                    "org.springframework.web..", "org.springframework.http..", "java..")
            .because("el adaptador REST no debe saltarse el servicio para llegar a los patrones o al repositorio");

    /**
     * Regla 3: toda clase Calculadora* es una estrategia de RF2.
     *
     * <p>Refinada respecto a la guia: la version original exigia que <em>todas</em> las clases del
     * paquete implementaran la interfaz, lo que falla con {@code SelectorCalculadora}, que vive en
     * el mismo paquete pero es el selector de estrategias, no una estrategia.
     */
    @ArchTest
    static final ArchRule calculadorasImplementanCalculadoraReembolso = classes()
            .that().resideInAPackage("..procesamiento.calculo..")
            .and().areNotInterfaces()
            .and().haveSimpleNameStartingWith("Calculadora")
            .should().implement(CalculadoraReembolso.class)
            .because("una categoria nueva se agrega como otra estrategia que el selector registra sola (Decision de diseno 2)");

    /** Regla 4: todo lo que vive en notificacion es un observador de RF3. */
    @ArchTest
    static final ArchRule notificadoresImplementanObservadorCambioEstado = classes()
            .that().resideInAPackage("..procesamiento.notificacion..")
            .and().areNotInterfaces()
            .should().implement(ObservadorCambioEstado.class)
            .because("el servicio notifica a List<ObservadorCambioEstado> sin conocer a cada interesado");
}
