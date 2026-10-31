# Proyecto Integrador: Sistema de Solicitudes de Reembolso de Gastos Corporativos

**Post-contenido — Unidad 12 · Patrones de Diseño de Software**
Juan Sebastián Gamboa Cristancho · Universidad de Santander (UDES) · Cúcuta, 2026

Sistema Spring Boot que reemplaza la hoja de cálculo del área de Finanzas para gestionar solicitudes de reembolso de gastos. Los empleados registran la categoría, el monto, la fecha del gasto y si adjuntaron los documentos soporte; el sistema valida la solicitud, calcula el monto aprobado y notifica a los interesados cada vez que la solicitud cambia de estado.

---

## Diseño Propuesto

*Sección escrita antes de implementar cualquier patrón (primer commit del repositorio). Para cada requisito se identifica primero qué varía, y solo después qué patrón lo resuelve, aplicando el test de explicabilidad de la Guía (sección 1.2): si la razón de un patrón no cabe en una oración, probablemente no se necesita.*

| Requisito | Punto de variabilidad concreto | Patrón propuesto |
|-----------|--------------------------------|------------------|
| **RF1** — Validación en varias etapas | El **número y el orden** de las verificaciones: hoy son tres (tope de la categoría, documentos soporte, duplicado en el período) y Finanzas ya anunció una cuarta (límite anual acumulado). Cada verificación es independiente y cualquiera puede detener la aprobación. | **Chain of Responsibility** |
| **RF2** — Cálculo distinto por categoría | La **fórmula del monto aprobado**, que depende de la categoría: tope diario × días (viaje), monto completo hasta un tope mensual (representación) y 80 % hasta un tope anual (capacitación). Habrá categorías nuevas, como «bienestar». | **Strategy** |
| **RF3** — Notificación simultánea | El **conjunto de interesados** y su forma de reaccionar a un cambio de estado: el empleado (todo resultado), Contabilidad (solo aprobaciones) y auditoría (todo, sin excepción). Ya se anunció un cuarto interesado: el panel de métricas. | **Observer** |

### Justificación de cada patrón (test de explicabilidad)

- **RF1 → Chain of Responsibility:** cada verificación es un eslabón que decide si la solicitud continúa o se rechaza sin conocer a los demás eslabones, de modo que el límite anual acumulado se agrega como un eslabón nuevo sin modificar los tres existentes.
  - *Por qué no otro:* Strategy elige **una** alternativa entre varias, pero aquí deben ejecutarse **todas** en secuencia. Decorator añadiría comportamiento alrededor de un objeto, pero no expresa «cortar el flujo al primer rechazo».
- **RF2 → Strategy:** cada categoría es un algoritmo intercambiable que produce el mismo resultado (el monto aprobado) con su propia fórmula, seleccionado una sola vez por la categoría de la solicitud, así que «bienestar» será una clase nueva y no una rama más en un `switch`.
  - *Por qué no otro:* State no aplica, porque la categoría no cambia durante la vida de la solicitud ni tiene transiciones. Template Method exigiría un esqueleto común que las tres fórmulas no comparten: no tienen pasos en común, solo la firma.
- **RF3 → Observer:** el cambio de estado es un evento con varios receptores, cada uno con su propia reacción, y el servicio central debe notificarlos sin conocerlos, para que el panel de métricas se sume como un suscriptor nuevo.
  - *Por qué no otro:* Mediator coordinaría interacciones **entre** los interesados, pero aquí los interesados no se comunican entre sí; solo reaccionan al mismo hecho.

### Criterios de composición consciente aplicados al conjunto

Los tres patrones conviven en el mismo flujo y cada uno pasa los criterios de la Guía:

- **Problema distinto:** validar, calcular y notificar son decisiones independientes; ningún patrón resuelve el problema de otro.
- **Variación real:** las tres variaciones están **documentadas en la especificación**, con al menos un caso futuro anunciado por Finanzas para cada una (límite anual acumulado, categoría «bienestar», panel de métricas). No son variaciones especulativas.
- **Interfaz natural:** la cadena de validación entrega una solicitud válida que la estrategia de cálculo consume, y el resultado persistido es lo que los observadores reciben. No hace falta ningún adaptador entre ellos.
- **Saturación:** son tres patrones, uno por requisito, coordinados en un único servicio de aplicación. Cualquier patrón adicional deberá pasar el mismo análisis antes de entrar al diseño.

## Estructura de paquetes prevista

```
src/main/java/com/empresa/reembolsos/
├── ReembolsosApplication.java
├── dominio/            ← SolicitudReembolso, CategoriaGasto, EstadoSolicitud (sin Spring ni JPA)
├── procesamiento/
│   ├── validacion/     ← RF1: Chain of Responsibility
│   ├── calculo/        ← RF2: Strategy
│   └── notificacion/   ← RF3: Observer
├── aplicacion/         ← SolicitudReembolsoService: único punto que coordina los tres patrones
├── infraestructura/    ← RepositorioSolicitudesJpa
└── adaptadores/rest/   ← SolicitudReembolsoController
```
