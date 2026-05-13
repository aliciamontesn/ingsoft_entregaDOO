# Foro de Desarrolladores — Grupo K

Backend del sistema de foro de desarrolladores, implementado como microservicios en Spring Boot.
Asignatura: Ingeniería del Software – Diseño Orientado a Objetos.

---

## Arquitectura general

El sistema sigue una arquitectura de **microservicios** con **CQRS** y **Event-Driven Architecture**.
Solo se implementan completamente dos microservicios; el resto se simula mediante *fakes* (ver más abajo).

```
┌─────────────────────────────────────────────────────┐
│  Cliente (curl / frontend futuro)                   │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP directo (sin API Gateway en esta entrega)
       ┌───────────┴──────────┐
       │                      │
       ▼                      ▼
┌─────────────┐       ┌──────────────────┐
│  servicio-  │──────▶│    servicio-     │
│ votaciones  │ PATCH │  publicaciones   │
│  :8082      │ score │     :8081        │
└─────────────┘       └──────────────────┘
       │                      │
       └──────────┬───────────┘
                  │ publish(evento)
                  ▼
         [FakeMessageBroker]
          (llamadas síncronas
           simulando async)
```

---

## Microservicios reales vs. fakes

| Microservicio | Estado | Puerto |
|---|---|---|
| **servicio-publicaciones** | Real (Spring Boot completo) | 8081 |
| **servicio-votaciones** | Real (Spring Boot completo) | 8082 |
| Servicio de Etiquetas | Fake | — |
| Servicio de Usuarios | Fake | — |
| Servicio de Reputación | Fake | — |
| Servicio de Notificaciones | Fake | — |
| Servicio de Búsqueda | Fake | — |
| Message Broker | Fake | — |
| Proyector CQRS | Fake | — |
| API Gateway | No implementado | — |

---

## Casos de uso implementados

| CU | Descripción | Microservicio |
|---|---|---|
| **CU1** | Emitir voto sobre una respuesta | servicio-votaciones |
| **CU2** | Reportar publicación para moderación | servicio-publicaciones |
| **CU3** | Publicar una pregunta | servicio-publicaciones |
| **CU4** | Marcar respuesta como aceptada | servicio-publicaciones |

---

## Estructura del código

```
repo/
├── backend/
│   ├── servicio-publicaciones/
│   │   └── src/main/java/com/grupok/publicaciones/
│   │       ├── controller/
│   │       │   ├── PreguntaController.java      # CU3: POST /preguntas
│   │       │   ├── ReporteController.java       # CU2: POST /publicaciones/{id}/reportes
│   │       │   ├── RespuestaController.java     # CU4: PATCH /respuestas/{id}/aceptar
│   │       │   ├── PublicacionController.java   # CU1: PATCH /publicaciones/{id}/score
│   │       │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│   │       ├── service/
│   │       │   ├── PreguntaService.java         # CU3
│   │       │   ├── ReporteService.java          # CU2
│   │       │   ├── RespuestaService.java        # CU4
│   │       │   └── PublicacionService.java      # CU1 (receptor del score)
│   │       ├── repository/
│   │       │   ├── PreguntaRepository.java
│   │       │   ├── PublicacionRepository.java   # + query custom findPreguntaByRespuestaId
│   │       │   ├── ReporteRepository.java       # + countByPublicacionId
│   │       │   └── RespuestaRepository.java
│   │       ├── model/
│   │       │   ├── Publicacion.java             # @Entity base (JOINED inheritance)
│   │       │   ├── Pregunta.java                # extends Publicacion
│   │       │   ├── Respuesta.java               # extends Publicacion, @ManyToOne Pregunta
│   │       │   └── Reporte.java                 # @Entity independiente
│   │       ├── dto/
│   │       │   ├── PublicarPreguntaRequest.java # record(usuarioId, titulo, contenido, etiquetaIds)
│   │       │   ├── ReportarPublicacionRequest.java # record(usuarioId, motivo)
│   │       │   └── AceptarRespuestaRequest.java # record(usuarioId)
│   │       └── fake/
│   │           ├── FakeMessageBroker.java       # enruta eventos a consumidores fake
│   │           ├── FakeServicioEtiquetas.java   # valida etiquetas (CU3, síncrono)
│   │           ├── FakeServicioUsuarios.java    # sumarPuntosReputacion (CU4, async)
│   │           ├── FakeServicioNotificaciones.java # notificarAutorRespuesta (CU4, async)
│   │           ├── FakeServicioBusqueda.java    # indexarEnElasticsearch (CU3, async)
│   │           └── FakeProyectorCQRS.java       # actualizarModeloLectura (CU3+CU4, async)
│   │
│   └── servicio-votaciones/
│       └── src/main/java/com/grupok/votaciones/
│           ├── controller/
│           │   ├── VotoController.java          # CU1: POST /votos
│           │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│           ├── service/
│           │   └── VotoService.java             # CU1
│           ├── repository/
│           │   └── VotoRepository.java          # + findByUsuarioIdAndRespuestaId
│           ├── model/
│           │   └── Voto.java
│           ├── dto/
│           │   └── EmitirVotoRequest.java       # record(usuarioId, respuestaId, valor)
│           ├── config/
│           │   └── RestTemplateConfig.java      # RestTemplate con soporte PATCH
│           └── fake/
│               ├── FakeMessageBroker.java       # enruta VotoEmitido a consumidores fake
│               ├── FakeServicioReputacion.java  # actualizarReputacion (CU1, async)
│               └── FakeServicioNotificaciones.java # notificarAutorVoto (CU1, async)
│
└── frontend/                                    # Reservado (vacío)
```

---

## Cómo funcionan los fakes

### El problema

El sistema real necesita varios microservicios independientes (Etiquetas, Usuarios,
Notificaciones, etc.) y un Message Broker real (Kafka). Desplegarlos todos para
implementar 4 casos de uso sería desproporcionado.

### La solución: clases fake inyectadas como @Component

Cada microservicio no implementado se reemplaza por una clase Java anotada con
`@Component` dentro del proyecto Spring Boot que lo necesita. Estas clases tienen
los mismos métodos que tendría el cliente HTTP real, pero en lugar de hacer una
llamada de red imprimen un mensaje en consola y devuelven una respuesta fija.

```java
// Ejemplo: FakeServicioEtiquetas en servicio-publicaciones
@Component
public class FakeServicioEtiquetas {
    public List<Long> validarEtiquetas(List<Long> etiquetaIds) {
        System.out.println("[FakeServicioEtiquetas] validarEtiquetas: " + etiquetaIds);
        return etiquetaIds; // siempre válidas
    }
}
```

Spring los inyecta exactamente igual que cualquier bean real, así que el código de
producción (`PreguntaService`, etc.) no sabe que está hablando con un fake.

### El FakeMessageBroker

En el sistema real, los servicios publican eventos en Kafka y los consumidores los
reciben de forma asíncrona. El `FakeMessageBroker` simula esto con llamadas síncronas
directas: cuando alguien llama a `publish(evento, id)`, el broker fake invoca
inmediatamente a los consumidores fake correspondientes.

```
Código real:
  fakeMessageBroker.publish("pregunta_publicada", pregunta.getId())

Lo que hace FakeMessageBroker internamente:
  fakeProyectorCQRS.actualizarModeloLectura("pregunta_publicada", id)
  fakeServicioBusqueda.indexarEnElasticsearch(id)
```

Hay un `FakeMessageBroker` en cada microservicio real, con los consumidores que
le corresponden según el diagrama de secuencia de cada caso de uso:

| Evento | FakeMessageBroker en | Consumidores fake invocados |
|---|---|---|
| `pregunta_publicada` | servicio-publicaciones | FakeProyectorCQRS, FakeServicioBusqueda |
| `respuesta_aceptada` | servicio-publicaciones | FakeServicioUsuarios, FakeProyectorCQRS, FakeServicioNotificaciones |
| `VotoEmitido` | servicio-votaciones | FakeServicioReputacion, FakeServicioNotificaciones |

### Comunicación real entre microservicios (CU1)

CU1 es el único caso de uso que requiere comunicación HTTP entre los dos microservicios
reales: al emitir un voto, `servicio-votaciones` necesita actualizar el score de la
respuesta en `servicio-publicaciones`.

Esto se hace con `RestTemplate` (configurado con `HttpComponentsClientHttpRequestFactory`
para soportar PATCH):

```
servicio-votaciones:VotoService
  → PATCH http://localhost:8081/publicaciones/{respuestaId}/score
  → servicio-publicaciones:PublicacionController
  → servicio-publicaciones:PublicacionService
```

---

## Modelo de datos (servicio-publicaciones)

```
Publicacion (tabla base, JOINED inheritance)
  ├── id          BIGINT PK
  ├── autor_id    BIGINT
  └── score       INT

Pregunta (extiende Publicacion)
  ├── titulo              VARCHAR
  ├── contenido           TEXT
  ├── accepted_answer_id  BIGINT (null si no hay respuesta aceptada)
  └── etiqueta_ids        @ElementCollection → tabla pregunta_etiqueta_ids

Respuesta (extiende Publicacion)
  ├── contenido    TEXT
  ├── es_aceptada  BOOLEAN
  └── pregunta_id  FK → Pregunta

Reporte
  ├── id              BIGINT PK
  ├── publicacion_id  BIGINT
  ├── usuario_id      BIGINT
  └── motivo          VARCHAR
```

JPA usa `InheritanceType.JOINED`: hay una tabla `publicacion` con los campos comunes
y tablas separadas `pregunta` y `respuesta` con sus campos específicos.

---

## API — Endpoints

### servicio-publicaciones (puerto 8081)

| Método | Ruta | Descripción | CU |
|---|---|---|---|
| `POST` | `/preguntas` | Publicar una pregunta | CU3 |
| `POST` | `/publicaciones/{id}/reportes` | Reportar una publicación | CU2 |
| `PATCH` | `/respuestas/{id}/aceptar` | Marcar respuesta como aceptada | CU4 |
| `PATCH` | `/publicaciones/{id}/score` | Actualizar score (llamado por votaciones) | CU1 |

#### POST /preguntas
```json
// Request
{ "usuarioId": 1, "titulo": "¿Cómo funciona JPA?", "contenido": "...", "etiquetaIds": [1, 2] }
// Response 201
{ "id": 42, "autorId": 1, "score": 0, "titulo": "...", "contenido": "...", "etiquetaIds": [1, 2] }
```

#### POST /publicaciones/{id}/reportes
```json
// Request
{ "usuarioId": 1, "motivo": "Contenido inapropiado" }
// Response 200
{ "id": 1, "publicacionId": 42, "usuarioId": 1, "motivo": "Contenido inapropiado" }
```

#### PATCH /respuestas/{id}/aceptar
```json
// Request
{ "usuarioId": 1 }
// Response 200 (sin cuerpo)
```

### servicio-votaciones (puerto 8082)

| Método | Ruta | Descripción | CU |
|---|---|---|---|
| `POST` | `/votos` | Emitir un voto sobre una respuesta | CU1 |

#### POST /votos
```json
// Request
{ "usuarioId": 1, "respuestaId": 10, "valor": 1 }   // valor: +1 o -1
// Response 200
{ "nuevoScore": 5 }
```

### Errores

Todos los errores devuelven JSON con el campo `error`:
```json
{ "error": "Descripción del error" }
```

| HTTP | Situación |
|---|---|
| 404 | Entidad no encontrada |
| 403 | El usuario no tiene permiso (no es el autor) |
| 409 | Conflicto: voto duplicado, o pregunta ya tiene respuesta aceptada |

---

## Continuidad diagrama → código

Los nombres de clases, métodos y rutas en el código corresponden exactamente con
los participantes y mensajes de los diagramas de secuencia de la Entrega 2:

| Participante en diagrama | Clase en código | Tipo Spring |
|---|---|---|
| `boundary "API de Votaciones"` | proyecto `servicio-votaciones` | — |
| `control ":VotoController"` | `VotoController` | `@RestController` |
| `control ":VotoService"` | `VotoService` | `@Service` |
| `entity ":VotoRepository"` | `VotoRepository` | `@Repository` |
| `boundary "API de Publicaciones"` | proyecto `servicio-publicaciones` | — |
| `control ":PreguntaController"` | `PreguntaController` | `@RestController` |
| `control ":PreguntaService"` | `PreguntaService` | `@Service` |
| `entity ":PreguntaRepository"` | `PreguntaRepository` | `@Repository` |
| `control ":ReporteController"` | `ReporteController` | `@RestController` |
| `control ":ReporteService"` | `ReporteService` | `@Service` |
| `control ":RespuestaController"` | `RespuestaController` | `@RestController` |
| `control ":RespuestaService"` | `RespuestaService` | `@Service` |
| `queue "Message Broker"` | `FakeMessageBroker` | `@Component` |
| `control ":ProyectorCQRS"` | `FakeProyectorCQRS` | `@Component` |
| `boundary "API de Etiquetas"` | `FakeServicioEtiquetas` | `@Component` |
| `boundary "API de Usuarios"` | `FakeServicioUsuarios` | `@Component` |
| `boundary "API de Notificaciones"` | `FakeServicioNotificaciones` | `@Component` |
| `boundary "API de Búsqueda"` | `FakeServicioBusqueda` | `@Component` |
| `boundary "API de Reputación"` | `FakeServicioReputacion` | `@Component` |

---

## Requisitos y puesta en marcha

### Prerrequisitos

- Java 21
- Maven 3.9+
- PostgreSQL 15+ corriendo en `localhost:5432`

### Crear las bases de datos

```sql
CREATE DATABASE foro_publicaciones;
CREATE DATABASE foro_votaciones;
```

Las tablas las crea Hibernate automáticamente al arrancar (`ddl-auto=update`).

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

Hay que arrancar `servicio-publicaciones` primero, ya que `servicio-votaciones`
le hace llamadas HTTP en CU1.
