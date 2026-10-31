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

---

## Decisiones de diseño

### Decisión 1 — Orden de la cadena de validación (RF1)

**Orden elegido:** `documentos → monto → duplicados`, ensamblado en un único lugar (`CadenaValidadores`).

| Eslabón | Qué hace | Costo |
|---------|----------|-------|
| `ValidadorDocumentosSoporte` | Lee un booleano de la solicitud | Constante: una comparación |
| `ValidadorMontoMaximo` | Pide el tope a la estrategia de la categoría y compara | Constante: búsqueda en un mapa y una comparación |
| `ValidadorDuplicados` | Recorre las solicitudes pendientes del empleado | Lineal: crece con el número de pendientes |

**Justificación (eficiencia):** la cadena se corta en el primer rechazo, así que conviene poner primero los eslabones baratos y que más rechazan, y dejar al final el más costoso. Con este orden, el recorrido de la lista solo se ejecuta para las solicitudes que ya tienen documentos y un monto dentro del tope. La prueba `CadenaValidadoresTest` lo comprueba contando cuántas veces se ejecuta el último eslabón.

- **Qué se gana:** las solicitudes sin documentos o con un monto excesivo, los errores más comunes al diligenciar el formulario, se rechazan sin recorrer la lista.
- **Qué se pierde:** si una solicitud incumple varias reglas, el empleado solo recibe el **primer** motivo. Al corregirlo puede descubrir el siguiente en un segundo intento. Ponerlo primero no evitaría esa pérdida, solo cambiaría cuál motivo se ve, y costaría recorrer la lista en todas las solicitudes, incluso las que se rechazarían por una regla trivial.
- **Cuándo reconsiderarlo:** si Finanzas pide informar **todos** los motivos a la vez, el problema deja de ser «cortar al primer rechazo» y la cadena debería acumular resultados en vez de detenerse.

### Decisión 2 — Selección de la estrategia de cálculo (RF2)

**Mecanismo elegido:** un `Map<CategoriaGasto, CalculadoraReembolso>` que `SelectorCalculadora` construye a partir de la `List<CalculadoraReembolso>` que Spring inyecta con todos los `@Component` registrados.

```java
public SelectorCalculadora(List<CalculadoraReembolso> lista) {
    this.calculadoras = lista.stream()
            .collect(Collectors.toMap(CalculadoraReembolso::getCategoria, Function.identity()));
}
```

**Justificación (RF2):** Finanzas planea agregar categorías nuevas sin modificar el código existente.

- Con el **mapa inyectado**, la categoría «bienestar» requiere un valor nuevo en el enum y una clase `CalculadoraBienestar` anotada con `@Component`. Spring la agrega a la lista y el selector la registra sola, sin tocar ninguna línea ya escrita del selector ni de las demás calculadoras. Cumple el principio Abierto/Cerrado.
- Con un **`switch` manual**, cada categoría nueva obliga a editar el método selector y a volver a probarlo, y es fácil olvidar un `case` (el error aparecería en tiempo de ejecución).
- **Costo aceptado:** el registro es implícito, porque no hay una lista visible de categorías en el código. Para compensarlo, `obtener()` falla con un mensaje claro (`Categoria no soportada: …`) si falta una estrategia, y `SelectorCalculadoraTest` cubre ese caso. Además, `Collectors.toMap` falla al iniciar la aplicación si dos calculadoras declaran la misma categoría, en vez de escoger una al azar.

### Hallazgos sobre el código de referencia de la guía

Al implementar se encontraron tres inconsistencias en el código de ejemplo de la guía, y cada una se corrigió:

1. **Tope de viaje contradictorio.** El `ValidadorMontoMaximo` de la guía usa un tope fijo de 150.000 para VIAJE, pero la calculadora reembolsa 150.000 **por día**. Un viaje de 3 días por 400.000 sería rechazado por la validación aunque el cálculo lo permite (tope real: 450.000). **Corrección:** cada estrategia expone `topeSolicitud(solicitud)` y el validador se lo pregunta a la estrategia de la categoría. Los topes quedan definidos en un solo lugar, y una categoría nueva no obliga a modificar el validador. Lo cubre el primer caso de `ValidadorMontoMaximoTest`.
2. **Duplicados que ignoran el año.** El `ValidadorDuplicados` de la guía compara solo `getMonth()`, de modo que una solicitud pendiente de octubre de 2025 bloquearía una de octubre de 2026. **Corrección:** se compara el período completo con `YearMonth` (mes y año). Lo cubre `ValidadorDuplicadosTest.apruebaElMismoMesDeOtroAnio`.
3. **Dominio «sin JPA» que JPA no puede persistir.** El checkpoint exige que el paquete `dominio` no importe nada de Spring ni de JPA, pero `RepositorioSolicitudesJpa extends JpaRepository<SolicitudReembolso, Long>` necesita que `SolicitudReembolso` sea una entidad. La guía no indica cómo conciliar ambas cosas. **Corrección:** el mapeo se declara en XML (`src/main/resources/META-INF/orm.xml`, con acceso por campos), que es parte del estándar JPA. La clase de dominio sigue siendo un objeto Java plano, y el detalle de persistencia (tabla, identificador autogenerado, enums guardados como texto) queda en la infraestructura.

---

## Ejecución

```bash
./mvnw test             # pruebas unitarias y de integración
./mvnw spring-boot:run  # API en http://localhost:8080
```

Crear una solicitud (RF1 valida y RF2 calcula el monto aprobado):

```bash
curl -X POST http://localhost:8080/api/solicitudes -H "Content-Type: application/json" \
  -d '{"empleadoId":"E-001","categoria":"VIAJE","montoSolicitado":400000,"fechaGasto":"2026-10-15","diasViaje":3,"documentosSoporte":true}'
# -> 200, "estado":"PENDIENTE", "montoAprobado":400000  (tope: 3 días x 150.000 = 450.000)
# Con 500000 respondería 400: "Monto excede el tope de la categoria VIAJE (450000)"
```

Cambiar su estado (RF3 notifica a los tres observadores):

```bash
curl -X PUT http://localhost:8080/api/solicitudes/1/estado -H "Content-Type: application/json" -d '"APROBADA"'
```

Una solicitud rechazada por la cadena de validación responde **400** con el motivo, en formato Problem Details:

```json
{ "status": 400, "title": "Solicitud de reembolso rechazada", "detail": "Faltan documentos soporte" }
```
