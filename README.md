# Foro de Desarrolladores - Grupo K

Sistema de foro técnico para desarrolladores, implementado como una aplicación web completa con arquitectura de microservicios en el backend y un frontend estático desplegado en producción.

Asignatura: Ingeniería del Software - Diseño Orientado a Objetos.

## Indice

1. [Descripcion general](#descripcion-general)
2. [Arquitectura](#arquitectura)
3. [Microservicios reales y fakes](#microservicios-reales-y-fakes)
4. [Casos de uso implementados](#casos-de-uso-implementados)
5. [Estructura del codigo](#estructura-del-codigo)
6. [Modelo de datos](#modelo-de-datos)
7. [API - Endpoints](#api---endpoints)
8. [Frontend](#frontend)
9. [Despliegue en produccion](#despliegue-en-produccion)
10. [Ejecucion en local](#ejecucion-en-local)
11. [Continuidad diagrama a codigo](#continuidad-diagrama-a-codigo)

---

## Descripcion general

El sistema permite a usuarios registrados publicar preguntas tecnicas, responderlas, votar respuestas, reportar contenido inapropiado y marcar respuestas como aceptadas. Cubre los cuatro casos de uso definidos en la especificacion del proyecto:

- **CU1**: Emitir voto sobre una respuesta
- **CU2**: Reportar publicacion para moderacion
- **CU3**: Publicar una pregunta
- **CU4**: Marcar respuesta como aceptada

---

## Arquitectura

El sistema sigue una arquitectura de **microservicios** con patrones **CQRS** y **Event-Driven Architecture**. Se implementan completamente dos microservicios backend; el resto de servicios del sistema se simulan mediante *fakes* (ver seccion correspondiente).

```
+-----------------------------------------------------+
|  Frontend (HTML/CSS/JS estatico - Netlify)          |
+------------------+----------------------------------+
                   |  HTTP / REST
       +-----------+-----------+
       |                       |
       v                       v
+-------------+       +------------------+
| servicio-   |       |   servicio-      |
| votaciones  +------>+  publicaciones   |
|   :8082     | PATCH |     :8081        |
+-------------+ score +------------------+
       |                       |
       +----------+------------+
                  | publish(evento)
                  v
         [FakeMessageBroker]
          (llamadas sincronas
           simulando async)
```

La comunicacion entre el frontend y los backends se realiza directamente via HTTP. No hay API Gateway implementado en esta entrega; los dos servicios exponen sus puertos directamente y el frontend tiene configuradas sus URLs en `config.js`.

---

## Microservicios reales y fakes

| Microservicio | Estado | Puerto |
|---|---|---|
| **servicio-publicaciones** | Real (Spring Boot 3.2.5) | 8081 |
| **servicio-votaciones** | Real (Spring Boot 3.2.5) | 8082 |
| Servicio de Etiquetas | Fake | - |
| Servicio de Usuarios y Reputacion | Fake | - |
| Servicio de Reputacion (votaciones) | Fake | - |
| Servicio de Notificaciones | Fake | - |
| Servicio de Busqueda | Fake | - |
| Message Broker | Fake | - |
| Proyector CQRS | Fake | - |
| API Gateway | No implementado | - |

### Como funcionan los fakes

Cada microservicio no implementado se reemplaza por una clase Java anotada con `@Component` dentro del proyecto Spring Boot que lo necesita. Estas clases tienen los mismos metodos que tendria el cliente HTTP real, pero en lugar de hacer llamadas de red imprimen trazas en consola y devuelven respuestas fijas.

Spring los inyecta exactamente igual que cualquier bean real, de modo que el codigo de produccion (`PreguntaService`, `VotoService`, etc.) no sabe si esta hablando con un servicio real o con un fake.

```java
// Ejemplo: FakeServicioEtiquetas en servicio-publicaciones
@Component
public class FakeServicioEtiquetas {
    public List<Long> validarEtiquetas(List<Long> etiquetaIds) {
        System.out.printf("[FakeServicioEtiquetas] validarEtiquetas: %s%n", etiquetaIds);
        return etiquetaIds;
    }
}
```

### El FakeMessageBroker

En el sistema real, los servicios publicarian eventos en Kafka y los consumidores los recibirian de forma asincrona. El `FakeMessageBroker` simula esto con llamadas sincronas directas: cuando se llama a `publish(evento, id)`, el broker fake invoca inmediatamente a los consumidores fake correspondientes.

Hay un `FakeMessageBroker` en cada microservicio real, con sus propios consumidores segun el diagrama de secuencia de cada caso de uso:

| Evento | FakeMessageBroker en | Consumidores fake invocados |
|---|---|---|
| `pregunta_publicada` | servicio-publicaciones | FakeProyectorCQRS, FakeServicioBusqueda |
| `respuesta_aceptada` | servicio-publicaciones | FakeServicioUsuarios, FakeProyectorCQRS, FakeServicioNotificaciones |
| `publicacion_ocultada` | servicio-publicaciones | FakeServicioBusqueda (eliminarDelIndice), FakeServicioNotificaciones (notificarAdministradores), FakeProyectorCQRS |
| `VotoEmitido` | servicio-votaciones | FakeServicioReputacion, FakeServicioNotificaciones |
| `VotoRetirado` | servicio-votaciones | FakeServicioReputacion |
| `VotoCambiado` | servicio-votaciones | FakeServicioReputacion |

---

## Casos de uso implementados

### CU1 - Emitir voto sobre una respuesta

Un usuario registrado emite un voto positivo (+1) o negativo (-1) sobre una respuesta publicada en el foro.

**Flujo principal:**

1. El usuario pulsa el boton de voto positivo o negativo sobre una respuesta.
2. El frontend envia `POST /votos` al servicio-votaciones con `usuarioId`, `respuestaId`, `valor` y `autorRespuestaId`.
3. El servicio verifica que el usuario no sea el autor de la respuesta (comprobacion local con `autorRespuestaId`).
4. Se consulta si el usuario ya tenia un voto previo sobre esa respuesta.
5. Se registra el voto y se actualiza el score en servicio-publicaciones via `PATCH /publicaciones/{id}/score`.
6. Se publica el evento `VotoEmitido` en el FakeMessageBroker.
7. FakeServicioReputacion actualiza la reputacion del autor de la respuesta.
8. FakeServicioNotificaciones notifica al autor del voto recibido.
9. El servicio devuelve el nuevo score, que el frontend muestra inmediatamente.

**Flujos alternativos:**

- **Autovoto (3b):** el servicio devuelve 403. La comprobacion se realiza con el campo `autorRespuestaId` enviado por el frontend, lo que evita dependencia de una llamada inter-servicio que podria fallar en produccion.
- **Mismo voto ya registrado (4a):** se elimina el voto existente (retraccion), se publica `VotoRetirado` y el score se ajusta.
- **Voto contrario ya registrado (4b):** se sustituye el voto en operacion atomica, se publica `VotoCambiado` y el score refleja el cambio completo (+2 o -2).

**Score como fuente de verdad:**

El score de cada respuesta se calcula siempre a partir de los votos almacenados en la base de datos de servicio-votaciones, a traves del endpoint `GET /votos/scores?ids=...`. Esto garantiza que el valor mostrado sea correcto incluso si el PATCH a servicio-publicaciones no esta disponible (por ejemplo, cuando `PUBLICACIONES_URL` no esta configurada).

---

### CU2 - Reportar publicacion para moderacion

Un usuario registrado reporta una pregunta o respuesta por contenido inapropiado.

**Flujo principal:**

1. El usuario pulsa el boton "Reportar" en una publicacion visible.
2. El frontend muestra un cuadro de texto para introducir el motivo.
3. El backend verifica que la publicacion existe, esta en estado VISIBLE y que el usuario no es su autor.
4. Se verifica que el usuario no ha reportado ya esa publicacion.
5. Se registra el reporte en base de datos.
6. Se cuenta el numero total de reportes acumulados para esa publicacion.
7. Si el numero de reportes es inferior al limite (3), la publicacion permanece visible y se confirma la accion al usuario indicando cuantos reportes quedan para el umbral.
8. Si se alcanza el limite, la publicacion se oculta automaticamente.

**Flujos alternativos:**

- **Publicacion eliminada o ya oculta (3a):** el backend devuelve 404. No se registra ningun reporte.
- **Autorreporte:** el backend devuelve 403. No se puede reportar la propia publicacion.
- **Reporte duplicado:** el backend devuelve 409. Un usuario no puede reportar la misma publicacion mas de una vez.
- **Limite alcanzado (6a):** la publicacion pasa a estado OCULTA, se publica el evento `publicacion_ocultada`, el FakeServicioBusqueda la elimina del indice de busqueda y el FakeServicioNotificaciones genera una alerta urgente para los administradores.

**Avisos progresivos en el frontend:**

El frontend muestra mensajes especificos segun los reportes restantes hasta el umbral:

- Con 2 reportes restantes: "Reporte enviado. Faltan 2 reportes para ocultar la publicacion."
- Con 1 reporte restante: "Reporte enviado. Con 1 reporte mas esta publicacion sera ocultada."
- Cuando se oculta: "La publicacion ha sido ocultada por exceso de reportes" y la pagina se recarga automaticamente.

**Efectos sobre la visibilidad:**

Las publicaciones en estado OCULTA no aparecen en el listado principal de preguntas ni en el detalle de sus respuestas. Si se intenta acceder directamente a una pregunta oculta por URL, el backend devuelve 403. Tampoco es posible publicar nuevas respuestas sobre una pregunta oculta.

---

### CU3 - Publicar una pregunta

Un usuario registrado publica una nueva pregunta con titulo, cuerpo y etiquetas.

**Flujo principal:**

1. El usuario rellena el formulario de nueva pregunta con titulo, contenido y una o varias etiquetas.
2. El frontend valida que titulo y contenido no esten vacios antes de enviar.
3. El backend valida las etiquetas con FakeServicioEtiquetas.
4. Se persiste la pregunta en PostgreSQL y se emite el evento `pregunta_publicada`.
5. FakeProyectorCQRS actualiza el modelo de lectura.
6. FakeServicioBusqueda indexa la pregunta en el motor de busqueda simulado.
7. El backend devuelve 201 Created con los datos de la pregunta creada.
8. El frontend redirige directamente al detalle de la nueva pregunta.

**Flujos alternativos:**

- **Etiquetas invalidas (4a):** FakeServicioEtiquetas devuelve error y el usuario debe corregirlas (en la implementacion actual el fake siempre valida, ya que las etiquetas son un conjunto fijo conocido).
- **Cancelacion:** el usuario puede abandonar el formulario en cualquier momento sin consecuencias.

**Etiquetas disponibles:**

| ID | Nombre |
|---|---|
| 1 | Java |
| 2 | Spring Boot |
| 3 | SQL |
| 4 | JPA |
| 5 | REST |
| 6 | Docker |
| 7 | Testing |
| 8 | Frontend |

---

### CU4 - Marcar respuesta como aceptada

El autor de una pregunta marca una de las respuestas como aceptada, indicando que resuelve su problema.

**Flujo principal:**

1. El autor de la pregunta visualiza las respuestas recibidas y pulsa "Aceptar" en una de ellas.
2. El backend verifica que el solicitante es el autor de la pregunta.
3. Se verifica que la pregunta no tiene ya una respuesta aceptada.
4. Se actualiza `esAceptada = true` en la respuesta y `acceptedAnswerId` en la pregunta.
5. Se emite el evento `respuesta_aceptada`.
6. FakeServicioUsuarios suma puntos de reputacion al autor de la respuesta.
7. FakeProyectorCQRS actualiza el modelo de lectura.
8. FakeServicioNotificaciones notifica al autor de la respuesta.
9. El frontend recarga el detalle de la pregunta mostrando el distintivo visual en la respuesta aceptada.

**Flujos alternativos:**

- **No es el autor (3a):** el backend devuelve 403.
- **Ya existe respuesta aceptada (5a):** el backend devuelve 409 con el mensaje "La pregunta ya tiene una respuesta aceptada. Puedes quitarla primero." El frontend muestra el boton "Quitar aceptada" sobre la respuesta actualmente aceptada.

**Desaceptar respuesta:**

El autor de la pregunta puede retirar la aceptacion de una respuesta pulsando "Quitar aceptada". El backend actualiza `esAceptada = false` y limpia `acceptedAnswerId`, publicando el evento `respuesta_desaceptada`. Esto permite posteriormente aceptar otra respuesta.

---

## Estructura del codigo

```
ingsoft_entregaDOO/
+-- backend/
|   +-- servicio-publicaciones/
|   |   +-- src/main/java/com/grupok/publicaciones/
|   |       +-- controller/
|   |       |   +-- PreguntaController.java        CU3: GET+POST /preguntas, DELETE /preguntas/{id}
|   |       |   +-- ReporteController.java         CU2: POST /publicaciones/{id}/reportes
|   |       |   +-- RespuestaController.java       CU4: POST/DELETE /respuestas, PATCH /aceptar, PATCH /desaceptar
|   |       |   +-- PublicacionController.java     CU1: PATCH /publicaciones/{id}/score
|   |       |   +-- GlobalExceptionHandler.java    @RestControllerAdvice, errores como {"error":"..."}
|   |       +-- service/
|   |       |   +-- PreguntaService.java           CU3: publicar, listar, obtenerDetalle, eliminar
|   |       |   +-- ReporteService.java            CU2: registrar reporte, ocultar si supera limite
|   |       |   +-- RespuestaService.java          CU4: aceptar, desaceptar, publicar, eliminar
|   |       |   +-- PublicacionService.java        CU1: actualizarScore (receptor del PATCH)
|   |       +-- repository/
|   |       |   +-- PreguntaRepository.java
|   |       |   +-- PublicacionRepository.java     query: findPreguntaByRespuestaId
|   |       |   +-- ReporteRepository.java         countByPublicacionId, existsByUsuarioIdAndPublicacionId
|   |       |   +-- RespuestaRepository.java       findByPreguntaId, countVisibleByPreguntaId
|   |       +-- model/
|   |       |   +-- Publicacion.java               @Entity base (JOINED inheritance)
|   |       |   +-- Pregunta.java                  extends Publicacion
|   |       |   +-- Respuesta.java                 extends Publicacion, @ManyToOne Pregunta
|   |       |   +-- Reporte.java                   @Entity independiente
|   |       |   +-- EstadoPublicacion.java         enum: VISIBLE, OCULTA, ELIMINADA
|   |       +-- dto/
|   |       |   +-- PublicarPreguntaRequest.java   record(usuarioId, titulo, contenido, etiquetaIds)
|   |       |   +-- PublicarRespuestaRequest.java  record(usuarioId, preguntaId, contenido)
|   |       |   +-- AceptarRespuestaRequest.java   record(usuarioId)
|   |       |   +-- ReportarPublicacionRequest.java  record(usuarioId, motivo)
|   |       |   +-- ReporteResultadoDto.java       record(numReportes, reportesRestantes, publicacionOculta)
|   |       |   +-- PreguntaResumenDto.java        para la lista de preguntas
|   |       |   +-- PreguntaDetalleDto.java        para el detalle (pregunta + respuestas)
|   |       +-- fake/
|   |       |   +-- FakeMessageBroker.java         enruta eventos a consumidores fake
|   |       |   +-- FakeServicioEtiquetas.java     validarEtiquetas (CU3, sincrono)
|   |       |   +-- FakeServicioUsuarios.java      sumarPuntosReputacion (CU4, asincrono)
|   |       |   +-- FakeServicioNotificaciones.java  notificarAutorRespuesta, notificarAdministradores
|   |       |   +-- FakeServicioBusqueda.java      indexarEnElasticsearch, eliminarDelIndice
|   |       |   +-- FakeProyectorCQRS.java         actualizarModeloLectura (CU3+CU4+CU2)
|   |       +-- config/
|   |       |   +-- CorsConfig.java               permite peticiones desde cualquier origen
|   |       +-- DataSourceConfig.java             configuracion HikariCP para Railway (DATABASE_URL)
|   |
|   +-- servicio-votaciones/
|       +-- src/main/java/com/grupok/votaciones/
|           +-- controller/
|           |   +-- VotoController.java           CU1: POST /votos, GET /votos/scores
|           |   +-- GlobalExceptionHandler.java   @RestControllerAdvice
|           +-- service/
|           |   +-- VotoService.java              CU1: emitirVoto, calcularScores
|           +-- repository/
|           |   +-- VotoRepository.java           findByUsuarioIdAndRespuestaId, findAllByRespuestaId, findAllByRespuestaIdIn
|           +-- model/
|           |   +-- Voto.java
|           +-- dto/
|           |   +-- EmitirVotoRequest.java        record(usuarioId, respuestaId, valor, autorRespuestaId)
|           +-- config/
|           |   +-- CorsConfig.java
|           |   +-- RestTemplateConfig.java       RestTemplate con soporte PATCH (HttpComponents)
|           +-- fake/
|               +-- FakeMessageBroker.java        enruta VotoEmitido/VotoRetirado/VotoCambiado
|               +-- FakeServicioReputacion.java   actualizarReputacion (CU1, asincrono)
|               +-- FakeServicioNotificaciones.java  notificarAutorVoto (CU1, asincrono)
|
+-- frontend/
    +-- index.html                               Listado de preguntas
    +-- pregunta.html                            Detalle de pregunta y respuestas
    +-- nueva-pregunta.html                      Formulario de nueva pregunta
    +-- login.html                               Identificacion por ID de usuario
    +-- css/
    |   +-- styles.css
    +-- js/
        +-- config.js                            URLs de los microservicios (local/produccion)
        +-- api.js                               Helper HTTP, funciones de auth, toasts
        +-- index.js                             Logica de la pagina principal
        +-- pregunta.js                          Logica del detalle: votar, aceptar, reportar, eliminar
        +-- nueva-pregunta.js                    Logica del formulario de nueva pregunta
```

---

## Modelo de datos

### servicio-publicaciones

```
Publicacion  (tabla base, InheritanceType.JOINED)
  id              BIGINT  PK
  autor_id        BIGINT
  score           INT     (actualizado por servicio-votaciones via PATCH)
  estado          VARCHAR (VISIBLE | OCULTA | ELIMINADA)
  fecha_creacion  TIMESTAMP

Pregunta  (extiende Publicacion)
  titulo              VARCHAR
  contenido           TEXT
  accepted_answer_id  BIGINT  (null si no hay respuesta aceptada)
  etiqueta_ids        @ElementCollection -> tabla pregunta_etiqueta_ids

Respuesta  (extiende Publicacion)
  contenido    TEXT
  es_aceptada  BOOLEAN
  pregunta_id  BIGINT  FK -> Pregunta

Reporte
  id              BIGINT  PK
  publicacion_id  BIGINT
  usuario_id      BIGINT
  motivo          VARCHAR
```

**Estados de publicacion:**

| Estado | Significado |
|---|---|
| VISIBLE | Publicacion accesible y visible para todos |
| OCULTA | Ocultada automaticamente por superar el limite de reportes; no se muestra ni en listados ni en detalle; no admite nuevos reportes ni nuevas respuestas |
| ELIMINADA | Borrada por su autor (soft delete); no se muestra ni se puede acceder |

### servicio-votaciones

```
Voto
  id            BIGINT  PK
  usuario_id    BIGINT
  respuesta_id  BIGINT
  valor         INT     (+1 o -1)
```

Un usuario solo puede tener un voto activo por respuesta. Si vota de nuevo con el mismo valor, el voto se retira. Si vota con el valor contrario, se sustituye atomicamente.

---

## API - Endpoints

### servicio-publicaciones (puerto 8081)

| Metodo | Ruta | Descripcion | CU |
|---|---|---|---|
| `GET` | `/preguntas` | Listar preguntas visibles con numero de respuestas | CU3 |
| `POST` | `/preguntas` | Publicar nueva pregunta | CU3 |
| `GET` | `/preguntas/{id}` | Detalle de pregunta con sus respuestas visibles | CU3/CU4 |
| `DELETE` | `/preguntas/{id}?usuarioId=X` | Eliminar pregunta (solo el autor) | - |
| `POST` | `/respuestas` | Publicar respuesta a una pregunta | - |
| `PATCH` | `/respuestas/{id}/aceptar` | Marcar respuesta como aceptada | CU4 |
| `PATCH` | `/respuestas/{id}/desaceptar` | Retirar aceptacion de una respuesta | CU4 ext.5a |
| `DELETE` | `/respuestas/{id}?usuarioId=X` | Eliminar respuesta (autor o autor de la pregunta) | - |
| `POST` | `/publicaciones/{id}/reportes` | Reportar una publicacion | CU2 |
| `PATCH` | `/publicaciones/{id}/score` | Actualizar score (llamado internamente por servicio-votaciones) | CU1 |

#### POST /preguntas

```json
// Request
{
  "usuarioId": 1,
  "titulo": "Como funciona JPA con herencia JOINED?",
  "contenido": "Estoy implementando...",
  "etiquetaIds": [2, 4]
}
// Response 201
{
  "id": 42,
  "autorId": 1,
  "score": 0,
  "estado": "VISIBLE",
  "titulo": "Como funciona JPA con herencia JOINED?",
  "contenido": "Estoy implementando...",
  "etiquetaIds": [2, 4],
  "fechaCreacion": "2026-05-24T10:30:00"
}
```

#### GET /preguntas/{id}

```json
// Response 200
{
  "pregunta": {
    "id": 42,
    "autorId": 1,
    "score": 0,
    "titulo": "...",
    "contenido": "...",
    "etiquetaIds": [2, 4],
    "acceptedAnswerId": null,
    "fechaCreacion": "2026-05-24T10:30:00",
    "estado": "VISIBLE"
  },
  "respuestas": [
    {
      "id": 7,
      "autorId": 3,
      "score": 2,
      "contenido": "...",
      "esAceptada": false,
      "preguntaId": 42,
      "estado": "VISIBLE"
    }
  ]
}
```

#### POST /publicaciones/{id}/reportes

```json
// Request
{ "usuarioId": 2, "motivo": "Contenido inapropiado" }
// Response 200
{
  "numReportes": 1,
  "reportesRestantes": 2,
  "publicacionOculta": false
}
```

#### PATCH /respuestas/{id}/aceptar y /desaceptar

```json
// Request
{ "usuarioId": 1 }
// Response 200 (sin cuerpo)
```

---

### servicio-votaciones (puerto 8082)

| Metodo | Ruta | Descripcion | CU |
|---|---|---|---|
| `POST` | `/votos` | Emitir voto sobre una respuesta | CU1 |
| `GET` | `/votos/scores?ids=1,2,3` | Obtener scores actuales de varias respuestas | CU1 |

#### POST /votos

```json
// Request
{
  "usuarioId": 2,
  "respuestaId": 7,
  "valor": 1,
  "autorRespuestaId": 3
}
// Response 200
{ "nuevoScore": 3 }
```

El campo `autorRespuestaId` es enviado por el frontend y permite al backend verificar el autovoto sin necesidad de una llamada adicional a servicio-publicaciones.

Comportamiento segun el estado previo del voto:

| Situacion | Resultado | Evento publicado |
|---|---|---|
| Sin voto previo | Se registra el voto | `VotoEmitido` |
| Mismo voto ya registrado | Se retira el voto | `VotoRetirado` |
| Voto contrario ya registrado | Se sustituye el voto | `VotoCambiado` |

#### GET /votos/scores?ids=1,2,3

```json
// Response 200
{
  "7": 3,
  "8": -1,
  "9": 0
}
```

El frontend llama a este endpoint cada vez que carga el detalle de una pregunta para mostrar los scores reales calculados desde los votos almacenados.

---

### Codigos de error

Todos los errores devuelven JSON con el campo `error`:

```json
{ "error": "Descripcion del error" }
```

| HTTP | Situacion |
|---|---|
| 400 | Datos invalidos en la peticion (campo obligatorio ausente, valor fuera de rango) |
| 403 | Sin permiso: autovoto, autorreporte, o accion reservada al autor |
| 404 | Entidad no encontrada o en estado ELIMINADA/OCULTA |
| 409 | Conflicto: voto duplicado, reporte duplicado, o pregunta ya tiene respuesta aceptada |
| 503 | servicio-publicaciones no accesible para verificar el autor (solo si `autorRespuestaId` no se envia) |

---

## Frontend

El frontend es una aplicacion de paginas estaticas en HTML, CSS y JavaScript vanilla, sin frameworks ni dependencias de build. Se comunica directamente con los dos microservicios via fetch API.

### Paginas

| Pagina | Archivo | Descripcion |
|---|---|---|
| Identificacion | `login.html` | El usuario introduce su ID numerico para identificarse (sin contrasena, sistema fake de autenticacion) |
| Listado de preguntas | `index.html` | Muestra todas las preguntas visibles con numero de respuestas, etiquetas, autor y fecha. Indica si la pregunta tiene respuesta aceptada. |
| Detalle de pregunta | `pregunta.html` | Muestra el contenido de la pregunta, sus respuestas con score y botones de accion, y el formulario para publicar una nueva respuesta |
| Nueva pregunta | `nueva-pregunta.html` | Formulario con titulo, contenido y selector de etiquetas |

### Funcionalidades del frontend

**Votacion:**
- Botones de voto positivo (+1) y negativo (-1) sobre cada respuesta
- El score se actualiza inmediatamente tras el voto sin recargar la pagina
- Al recargar, el score se obtiene desde servicio-votaciones (`GET /votos/scores`) como fuente de verdad
- El autor de una respuesta no puede votar su propia respuesta (bloqueado en backend y senalizado con error en frontend)
- Votar dos veces con el mismo valor retira el voto
- Votar con valor contrario cambia el voto

**Reportes:**
- Boton "Reportar" disponible en preguntas y respuestas
- Se solicita motivo mediante un cuadro de texto
- El frontend muestra avisos progresivos segun cuantos reportes quedan para el umbral de ocultado
- Si la publicacion es ocultada, la pagina se recarga automaticamente y desaparece

**Aceptar respuesta:**
- Solo visible para el autor de la pregunta, unicamente si no hay respuesta aceptada
- Tras aceptar, aparece el distintivo visual "Respuesta aceptada" y el boton "Quitar aceptada"
- El autor puede retirar la aceptacion para poder aceptar otra respuesta diferente

**Eliminar:**
- El autor de una pregunta puede eliminarla; se redirige al listado tras la eliminacion
- El autor de una respuesta, o el autor de la pregunta, pueden eliminar esa respuesta

**Identificacion:**
- Sistema fake basado en localStorage: el usuario introduce un ID numerico cualquiera
- Todas las paginas redirigen a login si no hay sesion activa
- Boton "Salir" en el encabezado de todas las paginas autenticadas

### Configuracion de URLs

Las URLs de los microservicios se configuran en `frontend/js/config.js`:

```javascript
// Para desarrollo local
window.CONF = {
  API_PUB: 'http://localhost:8081',
  API_VOT: 'http://localhost:8082'
};

// Para produccion (Railway)
window.CONF = {
  API_PUB: 'https://publicaciones-production.up.railway.app',
  API_VOT: 'https://votaciones-production.up.railway.app'
};
```

El resto del codigo JavaScript lee estas URLs desde `window.CONF`, de modo que cambiar el entorno solo requiere modificar este fichero.

---

## Despliegue en produccion

### Backend (Railway)

Ambos microservicios estan desplegados en Railway. Cada uno tiene su propia instancia de PostgreSQL gestionada por el plugin de Railway.

**Variables de entorno necesarias en Railway:**

Para servicio-publicaciones:

| Variable | Descripcion |
|---|---|
| `DATABASE_URL` | URL completa de PostgreSQL en formato `postgresql://user:pass@host:port/db` |
| `PORT` | Puerto asignado por Railway (se inyecta automaticamente) |

Para servicio-votaciones:

| Variable | Descripcion |
|---|---|
| `PGHOST` | Host de PostgreSQL (inyectado por el plugin) |
| `PGPORT` | Puerto de PostgreSQL (inyectado por el plugin) |
| `PGDATABASE` | Nombre de la base de datos (inyectado por el plugin) |
| `PGUSER` | Usuario de PostgreSQL (inyectado por el plugin) |
| `PGPASSWORD` | Contrasena de PostgreSQL (inyectado por el plugin) |
| `PUBLICACIONES_URL` | URL publica de servicio-publicaciones en Railway (necesaria para CU1) |
| `PORT` | Puerto asignado por Railway |

**Nota sobre `DATABASE_URL` en servicio-publicaciones:**

Railway inyecta la URL en formato `postgresql://user:pass@host:port/db`. El driver JDBC de PostgreSQL no acepta credenciales inlineadas en la URL. `DataSourceConfig.java` descompone la URL con `java.net.URI` y configura usuario y contrasena por separado en HikariCP.

**Nota sobre `PUBLICACIONES_URL` en servicio-votaciones:**

Si esta variable no esta configurada, el score de las respuestas en la base de datos de servicio-publicaciones no se actualizara. Sin embargo, el sistema no falla: los votos se siguen registrando correctamente y el score se calcula en tiempo real desde los votos de la base de datos de servicio-votaciones.

### Frontend (Netlify)

El frontend se despliega en Netlify como sitio estatico. Solo es necesario subir el contenido de la carpeta `frontend/` con la URL de produccion ya configurada en `config.js`.

---

## Ejecucion en local

### Prerrequisitos

- Java 21
- Maven 3.9 o superior
- PostgreSQL 15 o superior corriendo en `localhost:5432`

### Crear las bases de datos

```sql
CREATE DATABASE foro_publicaciones;
CREATE DATABASE foro_votaciones;
```

Las tablas las crea Hibernate automaticamente al arrancar cada servicio (`spring.jpa.hibernate.ddl-auto=update`).

### Arrancar servicio-publicaciones

```bash
cd backend/servicio-publicaciones
mvn spring-boot:run
# Escucha en http://localhost:8081
```

### Arrancar servicio-votaciones

```bash
cd backend/servicio-votaciones
mvn spring-boot:run
# Escucha en http://localhost:8082
```

Se recomienda arrancar `servicio-publicaciones` antes que `servicio-votaciones`, ya que este ultimo le hace llamadas HTTP para actualizar scores en CU1.

### Abrir el frontend

Abrir `frontend/index.html` directamente en el navegador o servir la carpeta con cualquier servidor HTTP estatico. Asegurarse de que `config.js` apunte a las URLs locales antes de abrir.

---

## Continuidad diagrama a codigo

Los nombres de clases, metodos y rutas en el codigo corresponden con los participantes y mensajes de los diagramas de secuencia de la especificacion del proyecto:

| Participante en diagrama | Clase en codigo | Tipo Spring |
|---|---|---|
| boundary "API de Votaciones" | proyecto servicio-votaciones | - |
| control ":VotoController" | `VotoController` | `@RestController` |
| control ":VotoService" | `VotoService` | `@Service` |
| entity ":VotoRepository" | `VotoRepository` | `@Repository` |
| boundary "API de Publicaciones" | proyecto servicio-publicaciones | - |
| control ":PreguntaController" | `PreguntaController` | `@RestController` |
| control ":PreguntaService" | `PreguntaService` | `@Service` |
| entity ":PreguntaRepository" | `PreguntaRepository` | `@Repository` |
| control ":ReporteController" | `ReporteController` | `@RestController` |
| control ":ReporteService" | `ReporteService` | `@Service` |
| control ":RespuestaController" | `RespuestaController` | `@RestController` |
| control ":RespuestaService" | `RespuestaService` | `@Service` |
| entity ":PublicacionRepository" | `PublicacionRepository` | `@Repository` |
| entity ":ReporteRepository" | `ReporteRepository` | `@Repository` |
| entity ":RespuestaRepository" | `RespuestaRepository` | `@Repository` |
| queue "Message Broker" | `FakeMessageBroker` | `@Component` |
| control ":ProyectorCQRS" | `FakeProyectorCQRS` | `@Component` |
| boundary "API de Etiquetas" | `FakeServicioEtiquetas` | `@Component` |
| boundary "API de Usuarios" | `FakeServicioUsuarios` | `@Component` |
| boundary "API de Notificaciones" (publicaciones) | `FakeServicioNotificaciones` (publicaciones) | `@Component` |
| boundary "API de Notificaciones" (votaciones) | `FakeServicioNotificaciones` (votaciones) | `@Component` |
| boundary "API de Busqueda" | `FakeServicioBusqueda` | `@Component` |
| boundary "API de Reputacion" | `FakeServicioReputacion` | `@Component` |
