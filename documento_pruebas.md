# Documento de Pruebas — Grupo K

Ingeniería del Software — Diseño Orientado a Objetos

---

Este documento recoge el análisis de pruebas realizado sobre los cuatro objetivos asignados al Grupo K: dos endpoints REST y dos métodos de servicio. Para cada uno se identifican las clases de equivalencia, se derivan los casos de prueba y, en los métodos con lógica de control, se calculan los caminos básicos.

Los tests están implementados en:
- `backend/servicio-publicaciones/src/test/` → `PreguntaIntegracionTest`, `PreguntaServiceTest`, `ReporteServiceTest`
- `backend/servicio-votaciones/src/test/` → `VotoIntegracionTest`

---

## Índice

1. [Clases de equivalencia](#1-clases-de-equivalencia)
2. [Casos de prueba derivados](#2-casos-de-prueba-derivados)
3. [Caminos básicos](#3-caminos-básicos)

---

## 1. Clases de equivalencia

Para cada objetivo se identifican las particiones de equivalencia: grupos de entradas que el sistema trata de la misma manera. Probar un valor de cada partición es equivalente a probar todos los de esa partición.

---

### 1.1 POST /preguntas

El endpoint valida el cuerpo con `@Valid`. Los campos `titulo` y `contenido` llevan `@NotBlank`; `usuarioId` lleva `@NotNull`. Si alguna validación falla, Spring devuelve 400 directamente sin llegar al servicio.

| ID | Parámetro | Descripción | Tipo | Ejemplo |
|----|-----------|-------------|------|---------|
| CE-P1 | `usuarioId` | Long no nulo | Válida | `10` |
| CE-P2 | `usuarioId` | null | Inválida | `null` |
| CE-P3 | `titulo` | Cadena con contenido | Válida | `"¿Cómo funciona JPA?"` |
| CE-P4 | `titulo` | Vacío o solo espacios | Inválida | `""` |
| CE-P5 | `contenido` | Cadena con contenido | Válida | `"Tengo este problema..."` |
| CE-P6 | `contenido` | Vacío o solo espacios | Inválida | `""` |
| CE-P7 | `etiquetaIds` | Lista con elementos | Válida | `[1, 2]` |
| CE-P8 | `etiquetaIds` | Lista vacía | Válida | `[]` |
| CE-P9 | `etiquetaIds` | null | Válida | `null` — el servicio lo trata igual que lista vacía |

---

### 1.2 POST /votos

El controlador valida `@NotNull` en `usuarioId` y `respuestaId`. La lógica semántica (valor debe ser ±1, no se puede votar la propia respuesta) la comprueba el servicio y devuelve 400 o 403 según el caso.

| ID | Parámetro | Descripción | Tipo | Ejemplo |
|----|-----------|-------------|------|---------|
| CE-V1 | `usuarioId` | Long no nulo | Válida | `10` |
| CE-V2 | `usuarioId` | null | Inválida | `null` |
| CE-V3 | `respuestaId` | Long no nulo | Válida | `45` |
| CE-V4 | `respuestaId` | null | Inválida | `null` |
| CE-V5 | `valor` | +1 o -1 | Válida | `1`, `-1` |
| CE-V6 | `valor` | Cualquier otro entero | Inválida | `0`, `2`, `-2` |
| CE-V7 | `autorRespuestaId` | Long distinto de `usuarioId` | Válida | `99` |
| CE-V8 | `autorRespuestaId` | Igual que `usuarioId` | Inválida — autovoto | mismo valor que `usuarioId` |

---

### 1.3 PreguntaService.obtenerDetalle(Long preguntaId)

El método tiene tres condiciones de guarda sobre el estado de la pregunta, y luego filtra las respuestas por estado. Las particiones se derivan de esas tres condiciones y de los posibles estados de las respuestas.

| ID | Variable | Descripción | Tipo | Ejemplo |
|----|----------|-------------|------|---------|
| CE-OD1 | `preguntaId` | ID que existe en base de datos | Válida | `1L` |
| CE-OD2 | `preguntaId` | ID que no existe | Inválida | `999L` |
| CE-OD3 | Estado de la pregunta | `VISIBLE` o `null` — se puede ver | Válida | `VISIBLE` |
| CE-OD4 | Estado de la pregunta | `ELIMINADA` — no existe para el usuario | Inválida | `ELIMINADA` |
| CE-OD5 | Estado de la pregunta | `OCULTA` — acceso denegado | Inválida | `OCULTA` |
| CE-OD6 | Estado de las respuestas | `VISIBLE` o `null` — se incluyen en el resultado | Válida | `VISIBLE`, `null` |
| CE-OD7 | Estado de las respuestas | `ELIMINADA` — se excluyen del resultado | Inválida | `ELIMINADA` |
| CE-OD8 | Estado de las respuestas | `OCULTA` — se excluyen del resultado | Inválida | `OCULTA` |

---

### 1.4 ReporteService.reportarPublicacion(Long usuarioId, Long publicacionId, String motivo)

El método tiene cinco condiciones de guarda secuenciales y una lógica de ocultación automática cuando se acumulan 3 reportes. Las particiones incluyen un análisis de valor frontera sobre ese contador (límite = 3).

| ID | Variable | Descripción | Tipo | Ejemplo |
|----|----------|-------------|------|---------|
| CE-R1 | `publicacionId` | Existe en BD y está VISIBLE | Válida | `100L` |
| CE-R2 | `publicacionId` | No existe en BD | Inválida | `999L` |
| CE-R3 | Estado de la publicación | `ELIMINADA` — se rechaza el reporte | Inválida | `ELIMINADA` |
| CE-R4 | Estado de la publicación | `OCULTA` — se rechaza el reporte | Inválida | `OCULTA` |
| CE-R5 | `usuarioId` vs `autorId` | El que reporta es distinto del autor | Válida | `usuarioId=5`, `autorId=10` |
| CE-R6 | `usuarioId` vs `autorId` | El que reporta es el propio autor | Inválida | `usuarioId=10`, `autorId=10` |
| CE-R7 | Reporte previo | No hay reporte anterior de este usuario | Válida | `false` |
| CE-R8 | Reporte previo | Ya había reportado esta publicación | Inválida | `true` |
| CE-R9 | `numReportes` (frontera) | 2 — justo por debajo del límite, no oculta | Válida | `2` |
| CE-R10 | `numReportes` (frontera) | 3 — en el límite, oculta automáticamente | Válida | `3` |

---

## 2. Casos de prueba derivados

Cada caso de prueba cubre al menos una clase de equivalencia. Se indica el nombre exacto del método de test en el código para facilitar la trazabilidad.

---

### 2.1 POST /preguntas — `PreguntaIntegracionTest`

| CT | Clases cubiertas | Descripción | Resultado esperado | Método en el test |
|----|-----------------|-------------|-------------------|--------------------|
| CT-P1 | CE-P1, CE-P3, CE-P5, CE-P7 | Petición completa y válida | `201 Created` | `publicarPregunta_CuandoDatosSonValidos_DebeRetornar201Created` |
| CT-P2 | CE-P4, CE-P6 | `titulo` y `contenido` vacíos | `400 Bad Request` | `publicarPregunta_CuandoTituloOContenidoSonVacios_DebeRetornar400BadRequest` |

---

### 2.2 POST /votos — `VotoIntegracionTest`

| CT | Clases cubiertas | Descripción | Resultado esperado | Método en el test |
|----|-----------------|-------------|-------------------|--------------------|
| CT-V1 | CE-V1, CE-V3, CE-V5, CE-V7 | Voto +1 con todos los campos válidos | `200 OK` con `nuevoScore` en el cuerpo | `emitirVoto_CuandoDatosSonValidos_DebeRetornar200YContenerNuevoScore` |
| CT-V2 | CE-V2, CE-V4 | Cuerpo vacío `{}` — `usuarioId` y `respuestaId` son null | `400 Bad Request` | `emitirVoto_CuandoFaltanCamposObligatorios_DebeRetornar400BadRequest` |

---

### 2.3 PreguntaService.obtenerDetalle — `PreguntaServiceTest`

| CT | Clases cubiertas | Descripción | Resultado esperado | Método en el test |
|----|-----------------|-------------|-------------------|--------------------|
| CT-OD1 | CE-OD2 | La pregunta no existe en BD | `ResponseStatusException` 404 | `obtenerDetalle_CuandoPreguntaNoExiste_DebeLanzarNotFound` |
| CT-OD2 | CE-OD1, CE-OD4 | La pregunta existe pero está ELIMINADA | `ResponseStatusException` 404 | `obtenerDetalle_CuandoPreguntaEstaEliminada_DebeLanzarNotFound` |
| CT-OD3 | CE-OD1, CE-OD5 | La pregunta existe pero está OCULTA | `ResponseStatusException` 403 | `obtenerDetalle_CuandoPreguntaEstaOculta_DebeLanzarForbidden` |
| CT-OD4 | CE-OD1, CE-OD3 | Pregunta VISIBLE sin respuestas | `PreguntaDetalleDto` no nulo con lista vacía | `obtenerDetalle_FlujoFeliz_SinRespuestas` |
| CT-OD5 | CE-OD1, CE-OD3, CE-OD6, CE-OD7, CE-OD8 | Pregunta VISIBLE con respuestas en los cuatro estados posibles | Solo se devuelven las VISIBLE y null | `obtenerDetalle_FlujoFeliz_ConRespuestasFiltradas` |
| CT-OD6 | CE-OD1, CE-OD3, CE-OD6 | Respuesta con `estado=null` — debe incluirse, no filtrarse | `PreguntaDetalleDto` no nulo | `obtenerDetalle_ConRespuestaEstadoNulo_DebeTratarlaComoVisible` |

---

### 2.4 ReporteService.reportarPublicacion — `ReporteServiceTest`

| CT | Clases cubiertas | Descripción | Resultado esperado | Método en el test |
|----|-----------------|-------------|-------------------|--------------------|
| CT-R1 | CE-R2 | La publicación no existe | `ResponseStatusException` 404 | `reportarPublicacion_CuandoNoExiste_DebeLanzarNotFound` |
| CT-R2 | CE-R1, CE-R3 | La publicación está ELIMINADA | `ResponseStatusException` 404 | `reportarPublicacion_CuandoEstaEliminada_DebeLanzarNotFound` |
| CT-R3 | CE-R1, CE-R4 | La publicación está OCULTA | `ResponseStatusException` 404 | `reportarPublicacion_CuandoEstaOculta_DebeLanzarNotFound` |
| CT-R4 | CE-R1, CE-R6 | El usuario intenta reportar su propia publicación | `ResponseStatusException` 403 | `reportarPublicacion_CuandoEsAutor_DebeLanzarForbidden` |
| CT-R5 | CE-R1, CE-R5, CE-R8 | El usuario ya había reportado esta publicación antes | `ResponseStatusException` 409 | `reportarPublicacion_CuandoYaExisteUnReporteDelMismoUsuario_DebeLanzarConflict` |
| CT-R6 | CE-R1, CE-R5, CE-R7, CE-R9 | Flujo exitoso, `numReportes=2` — frontera inferior, no oculta | `ReporteResultadoDto` con `oculta=false`, sin evento publicado | `reportarPublicacion_FlujoExitoso_EnValorFrontera_DosReportesNoOculta` |
| CT-R7 | CE-R1, CE-R5, CE-R7, CE-R10 | Flujo exitoso, `numReportes=3` — en el límite, oculta | `ReporteResultadoDto` con `oculta=true`, evento `publicacion_ocultada` publicado | `reportarPublicacion_FlujoExitoso_AlcanzaLimiteDeTresYSeOculta` |

---

## 3. Caminos básicos

Los caminos básicos (técnica de McCabe) se calculan sobre los dos métodos de servicio, que son los que tienen ramificaciones condicionales. Para los endpoints no se aplica porque la lógica de control está en el servicio, no en el controlador.

El proceso es: dibujar el grafo de flujo de control, calcular la complejidad ciclomática V(G) y enumerar tantos caminos independientes como indica V(G). Cada camino debe estar cubierto por al menos un test.

---

### 3.1 PreguntaService.obtenerDetalle

**Grafo de flujo de control:**

```
N1 (entrada)
  |
N2: findById(preguntaId) ── no encontrada ──> N3: throw 404 (fin)
  |
  encontrada
  |
N4: ¿estado == ELIMINADA? ── sí ──> N5: throw 404 (fin)
  |
  no
  |
N6: ¿estado == OCULTA? ── sí ──> N7: throw 403 (fin)
  |
  no
  |
N8: filtrar respuestas (excluye ELIMINADA y OCULTA)
  |
N9: return PreguntaDetalleDto (fin)
```

**Complejidad ciclomática:**

Hay 3 decisiones binarias en el método (D1, D2, D3), por lo que:

**V(G) = número de decisiones + 1 = 3 + 1 = 4**

Usando la fórmula del grafo: V(G) = E − N + 2 = 10 − 9 + 2 = 4 ✓

**Caminos básicos:**

| Camino | Condiciones recorridas | Qué hace | Test |
|--------|----------------------|----------|------|
| P1 | D1 = falso | No encuentra la pregunta → lanza 404 | CT-OD1 |
| P2 | D1 = verdad, D2 = verdad | Pregunta existe pero ELIMINADA → lanza 404 | CT-OD2 |
| P3 | D1 = verdad, D2 = falso, D3 = verdad | Pregunta existe pero OCULTA → lanza 403 | CT-OD3 |
| P4 | D1 = verdad, D2 = falso, D3 = falso | Pregunta visible → filtra respuestas y devuelve DTO | CT-OD4, CT-OD5, CT-OD6 |

---

### 3.2 ReporteService.reportarPublicacion

**Grafo de flujo de control:**

```
N1 (entrada)
  |
N2: findById(publicacionId) ── no encontrada ──> N3: throw 404 (fin)
  |
  encontrada
  |
N4: ¿estado == ELIMINADA? ── sí ──> N5: throw 404 (fin)
  |
  no
  |
N6: ¿estado == OCULTA? ── sí ──> N7: throw 404 (fin)
  |
  no
  |
N8: ¿usuarioId == autorId? ── sí ──> N9: throw 403 (fin)
  |
  no
  |
N10: ¿ya existe reporte del usuario? ── sí ──> N11: throw 409 (fin)
  |
  no
  |
N12: guardar el reporte
  |
N13: contar reportes totales de la publicación
  |
N14: ¿numReportes >= 3? ── sí ──> N15: ocultar + publicar evento
  |                                      |
  no                                     |
  |<──────────────────────────────────────
N16: return ReporteResultadoDto (fin)
```

**Complejidad ciclomática:**

Hay 6 decisiones binarias (D1 a D6), por lo que:

**V(G) = número de decisiones + 1 = 6 + 1 = 7**

Usando la fórmula del grafo: V(G) = E − N + 2 = 21 − 16 + 2 = 7 ✓

**Caminos básicos:**

| Camino | Condiciones recorridas | Qué hace | Test |
|--------|----------------------|----------|------|
| P1 | D1 = falso | Publicación no encontrada → lanza 404 | CT-R1 |
| P2 | D1 = verdad, D2 = verdad | Publicación ELIMINADA → lanza 404 | CT-R2 |
| P3 | D1 = verdad, D2 = falso, D3 = verdad | Publicación OCULTA → lanza 404 | CT-R3 |
| P4 | D1 = verdad, D2 = falso, D3 = falso, D4 = verdad | El reporter es el autor → lanza 403 | CT-R4 |
| P5 | D1 = verdad, D2 = falso, D3 = falso, D4 = falso, D5 = verdad | Reporte duplicado → lanza 409 | CT-R5 |
| P6 | D1 = verdad, D2 = falso, D3 = falso, D4 = falso, D5 = falso, D6 = falso | Reporte guardado, menos de 3 en total → no oculta | CT-R6 |
| P7 | D1 = verdad, D2 = falso, D3 = falso, D4 = falso, D5 = falso, D6 = verdad | Reporte guardado, llega a 3 → oculta la publicación | CT-R7 |

---

*Repositorio: https://github.com/aliciamontesn/ingsoft_entregaDOO*  
*Para ejecutar los tests y generar el informe de cobertura: `mvn test` en cada servicio del backend. El informe JaCoCo se genera en `target/site/jacoco/index.html`.*
