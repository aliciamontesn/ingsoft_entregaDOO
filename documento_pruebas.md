# Documento de Pruebas - Grupo K

Ingeniería del Software - Diseño Orientado a Objetos  
Testing sobre los cuatro objetivos asignados al Grupo K.

---

## Índice

1. [Clases de equivalencia](#1-clases-de-equivalencia)
2. [Casos de prueba derivados](#2-casos-de-prueba-derivados)
3. [Caminos básicos](#3-caminos-básicos)

---

## 1. Clases de equivalencia

Se aplica partición de equivalencia sobre las entradas de cada objetivo. Las clases válidas agrupan entradas que el sistema debe procesar correctamente; las inválidas agrupan entradas que el sistema debe rechazar.

---

### 1.1 POST /preguntas

El endpoint recibe un JSON con los campos `usuarioId`, `titulo`, `contenido` y `etiquetaIds`. La validación de formato la hace Jakarta Validation (`@NotNull`, `@NotBlank`) antes de llegar al servicio.

| ID | Parámetro | Descripción de la clase | Tipo | Valor representativo |
|----|-----------|------------------------|------|----------------------|
| CE-P1 | `usuarioId` | Long no nulo | Válida | `10` |
| CE-P2 | `usuarioId` | null | Inválida | `null` |
| CE-P3 | `titulo` | Cadena no vacía | Válida | `"¿Cómo funciona JPA?"` |
| CE-P4 | `titulo` | Cadena vacía o solo espacios | Inválida | `""` |
| CE-P5 | `contenido` | Cadena no vacía | Válida | `"Descripción detallada..."` |
| CE-P6 | `contenido` | Cadena vacía o solo espacios | Inválida | `""` |
| CE-P7 | `etiquetaIds` | Lista con elementos | Válida | `[1, 2]` |
| CE-P8 | `etiquetaIds` | Lista vacía | Válida | `[]` |
| CE-P9 | `etiquetaIds` | null | Válida | `null` (el servicio lo inicializa a lista vacía) |

---

### 1.2 POST /votos

El endpoint recibe `usuarioId`, `respuestaId`, `valor` y `autorRespuestaId`. La validación de formato la hace Jakarta Validation; la validación semántica (valor ±1, no autovoto) la hace el servicio.

| ID | Parámetro | Descripción de la clase | Tipo | Valor representativo |
|----|-----------|------------------------|------|----------------------|
| CE-V1 | `usuarioId` | Long no nulo | Válida | `10` |
| CE-V2 | `usuarioId` | null | Inválida | `null` |
| CE-V3 | `respuestaId` | Long no nulo | Válida | `45` |
| CE-V4 | `respuestaId` | null | Inválida | `null` |
| CE-V5 | `valor` | +1 o -1 | Válida | `1`, `-1` |
| CE-V6 | `valor` | Distinto de ±1 | Inválida | `0`, `2`, `-2` |
| CE-V7 | `autorRespuestaId` | Long distinto de `usuarioId` | Válida | `99` |
| CE-V8 | `autorRespuestaId` | Igual que `usuarioId` (autovoto) | Inválida | mismo valor que `usuarioId` |

---

### 1.3 PreguntaService.obtenerDetalle(Long preguntaId)

Método de servicio. Las clases de equivalencia se derivan del estado de la pregunta en base de datos y del estado de sus respuestas.

| ID | Variable | Descripción de la clase | Tipo | Valor representativo |
|----|----------|------------------------|------|----------------------|
| CE-OD1 | `preguntaId` | ID que existe en BD | Válida | `1L` |
| CE-OD2 | `preguntaId` | ID que no existe en BD | Inválida | `999L` |
| CE-OD3 | Estado de la pregunta | `null` o `VISIBLE` | Válida | `VISIBLE` |
| CE-OD4 | Estado de la pregunta | `ELIMINADA` | Inválida | `ELIMINADA` |
| CE-OD5 | Estado de la pregunta | `OCULTA` | Inválida | `OCULTA` |
| CE-OD6 | Estado de las respuestas | `VISIBLE` o `null` | Válida (se incluyen) | `VISIBLE`, `null` |
| CE-OD7 | Estado de las respuestas | `ELIMINADA` | Inválida (se excluyen) | `ELIMINADA` |
| CE-OD8 | Estado de las respuestas | `OCULTA` | Inválida (se excluyen) | `OCULTA` |

---

### 1.4 ReporteService.reportarPublicacion(Long usuarioId, Long publicacionId, String motivo)

Método de servicio. Las clases de equivalencia reflejan las distintas condiciones de guarda del método y el valor frontera del contador de reportes (límite = 3).

| ID | Variable | Descripción de la clase | Tipo | Valor representativo |
|----|----------|------------------------|------|----------------------|
| CE-R1 | `publicacionId` | ID que existe en BD, estado VISIBLE | Válida | `100L` |
| CE-R2 | `publicacionId` | ID que no existe en BD | Inválida | `999L` |
| CE-R3 | Estado de la publicación | `ELIMINADA` | Inválida | `ELIMINADA` |
| CE-R4 | Estado de la publicación | `OCULTA` | Inválida | `OCULTA` |
| CE-R5 | `usuarioId` vs `autorId` | Reporter es distinto del autor | Válida | `usuarioId=5`, `autorId=10` |
| CE-R6 | `usuarioId` vs `autorId` | Reporter es el propio autor | Inválida | `usuarioId=10`, `autorId=10` |
| CE-R7 | Reporte previo | No existe reporte previo del usuario | Válida | `existsByUsuarioId... = false` |
| CE-R8 | Reporte previo | Ya existe reporte del mismo usuario | Inválida | `existsByUsuarioId... = true` |
| CE-R9 | `numReportes` (valor frontera) | Justo por debajo del límite (< 3) | Válida — no oculta | `2` |
| CE-R10 | `numReportes` (valor frontera) | En el límite o por encima (≥ 3) | Válida — oculta | `3` |

---

## 2. Casos de prueba derivados

Cada caso de prueba cubre al menos una clase de equivalencia. Los casos del tipo "camino feliz" combinan todas las clases válidas; los casos de error ejercitan una clase inválida cada uno.

---

### 2.1 POST /preguntas - PreguntaIntegracionTest

| CT | Clases cubiertas | Descripción | Resultado esperado |
|----|-----------------|-------------|-------------------|
| CT-P1 | CE-P1, CE-P3, CE-P5, CE-P7 | Petición válida: todos los campos correctos | `201 Created` |
| CT-P2 | CE-P4, CE-P6 | Titulo y contenido vacíos | `400 Bad Request` |

---

### 2.2 POST /votos - VotoIntegracionTest

| CT | Clases cubiertas | Descripción | Resultado esperado |
|----|-----------------|-------------|-------------------|
| CT-V1 | CE-V1, CE-V3, CE-V5, CE-V7 | Petición válida: voto +1 con autorRespuestaId distinto | `200 OK` con campo `nuevoScore` en el cuerpo |
| CT-V2 | CE-V2, CE-V4 | Cuerpo vacío `{}`: usuarioId y respuestaId son null | `400 Bad Request` |

---

### 2.3 PreguntaService.obtenerDetalle - PreguntaServiceTest

| CT | Clases cubiertas | Descripción | Resultado esperado |
|----|-----------------|-------------|-------------------|
| CT-OD1 | CE-OD2 | preguntaId no existe en BD | `ResponseStatusException` 404 |
| CT-OD2 | CE-OD1, CE-OD4 | Pregunta existe pero está ELIMINADA | `ResponseStatusException` 404 |
| CT-OD3 | CE-OD1, CE-OD5 | Pregunta existe pero está OCULTA | `ResponseStatusException` 403 |
| CT-OD4 | CE-OD1, CE-OD3 | Pregunta VISIBLE sin respuestas | `PreguntaDetalleDto` no nulo, lista vacía |
| CT-OD5 | CE-OD1, CE-OD3, CE-OD6, CE-OD7, CE-OD8 | Pregunta VISIBLE con respuestas en todos los estados (VISIBLE, null, ELIMINADA, OCULTA) | `PreguntaDetalleDto` con solo las VISIBLE y null |
| CT-OD6 | CE-OD1, CE-OD3, CE-OD6 | Respuesta con estado null debe incluirse como visible | `PreguntaDetalleDto` no nulo |

---

### 2.4 ReporteService.reportarPublicacion - ReporteServiceTest

| CT | Clases cubiertas | Descripción | Resultado esperado |
|----|-----------------|-------------|-------------------|
| CT-R1 | CE-R2 | publicacionId no existe | `ResponseStatusException` 404 |
| CT-R2 | CE-R1, CE-R3 | Publicación ELIMINADA | `ResponseStatusException` 404 |
| CT-R3 | CE-R1, CE-R4 | Publicación OCULTA | `ResponseStatusException` 404 |
| CT-R4 | CE-R1, CE-R6 | Usuario == autor (autorreporte) | `ResponseStatusException` 403 |
| CT-R5 | CE-R1, CE-R5, CE-R8 | Ya existe un reporte del mismo usuario | `ResponseStatusException` 409 |
| CT-R6 | CE-R1, CE-R5, CE-R7, CE-R9 | Flujo exitoso, numReportes=2 (frontera inferior) | `ReporteResultadoDto` con `oculta=false`, sin evento |
| CT-R7 | CE-R1, CE-R5, CE-R7, CE-R10 | Flujo exitoso, numReportes=3 (en el límite) | `ReporteResultadoDto` con `oculta=true`, evento publicado |

---

## 3. Caminos básicos

Se aplica la técnica de caminos básicos (McCabe) sobre los dos métodos de servicio, que son los que tienen lógica de control no trivial. Para cada método se calcula la complejidad ciclomática V(G) y se enumeran los caminos independientes.

---

### 3.1 PreguntaService.obtenerDetalle

**Grafo de flujo de control:**

```
N1 (entrada)
  |
N2: findById(preguntaId) ─ vacío ─> N3: throw 404 (salida)
  |
  encontrada
  |
N4: ¿estado == ELIMINADA? ─ sí ─> N5: throw 404 (salida)
  |
  no
  |
N6: ¿estado == OCULTA? ─ sí ─> N7: throw 403 (salida)
  |
  no
  |
N8: filtrar respuestas (excluye ELIMINADA y OCULTA)
  |
N9: return PreguntaDetalleDto (salida)
```

**Complejidad ciclomática:**

- Nodos (N): 9
- Aristas (E): 10 — N1→N2, N2→N3, N2→N4, N4→N5, N4→N6, N6→N7, N6→N8, N8→N9
- V(G) = E − N + 2 = 10 − 9 + 2 = **4** (equivalente a número de decisiones + 1 = 3 + 1 = 4)

**Caminos básicos (4):**

| Camino | Decisiones | Descripción | Test que lo cubre |
|--------|-----------|-------------|-------------------|
| P1 | D1=falso | pregunta no encontrada → lanza 404 | CT-OD1 |
| P2 | D1=verdad, D2=verdad | encontrada pero ELIMINADA → lanza 404 | CT-OD2 |
| P3 | D1=verdad, D2=falso, D3=verdad | encontrada pero OCULTA → lanza 403 | CT-OD3 |
| P4 | D1=verdad, D2=falso, D3=falso | encontrada y VISIBLE → retorna DTO | CT-OD4, CT-OD5, CT-OD6 |

---

### 3.2 ReporteService.reportarPublicacion

**Grafo de flujo de control:**

```
N1 (entrada)
  |
N2: findById(publicacionId) - vacío ─> N3: throw 404 (salida)
  |
  encontrada
  |
N4: ¿estado == ELIMINADA? ─ sí ─> N5: throw 404 (salida)
  |
  no
  |
N6: ¿estado == OCULTA? ─ sí ─> N7: throw 404 (salida)
  |
  no
  |
N8: ¿usuarioId == autorId? ─ sí ─> N9: throw 403 (salida)
  |
  no
  |
N10: ¿existeReportePrevio? ─ sí ─> N11: throw 409 (salida)
  |
  no
  |
N12: save(reporte)
  |
N13: countByPublicacionId
  |
N14: ¿numReportes >= 3? ─ sí ─> N15: ocultar publicación + publicar evento
  |                                    |
  no                                   |
  |<────────────────────────────────────
N16: return ReporteResultadoDto (salida)
```

**Complejidad ciclomática:**

- Nodos (N): 16
- Aristas (E): 21
- V(G) = E − N + 2 = 21 − 16 + 2 = **7** (equivalente a número de decisiones + 1 = 6 + 1 = 7)

**Caminos básicos (7):**

| Camino | Decisiones | Descripción | Test que lo cubre |
|--------|-----------|-------------|-------------------|
| P1 | D1=falso | publicación no encontrada → lanza 404 | CT-R1 |
| P2 | D1=verdad, D2=verdad | publicación ELIMINADA → lanza 404 | CT-R2 |
| P3 | D1=verdad, D2=falso, D3=verdad | publicación OCULTA → lanza 404 | CT-R3 |
| P4 | D1=verdad, D2=falso, D3=falso, D4=verdad | reporter == autor → lanza 403 | CT-R4 |
| P5 | D1=verdad, D2=falso, D3=falso, D4=falso, D5=verdad | reporte duplicado → lanza 409 | CT-R5 |
| P6 | D1=verdad, D2=falso, D3=falso, D4=falso, D5=falso, D6=falso | numReportes < 3 → retorna DTO sin ocultar | CT-R6 |
| P7 | D1=verdad, D2=falso, D3=falso, D4=falso, D5=falso, D6=verdad | numReportes ≥ 3 → oculta publicación y retorna DTO | CT-R7 |

---

*Los tests implementados están en el repositorio bajo `backend/servicio-publicaciones/src/test/` y `backend/servicio-votaciones/src/test/`. Ejecutar `mvn test` en cada servicio genera el informe de cobertura JaCoCo en `target/site/jacoco/index.html`.*
