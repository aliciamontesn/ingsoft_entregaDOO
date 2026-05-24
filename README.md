# Foro de Desarrolladores - Grupo K

Trabajo final de Ingenieria del Software - Disenio Orientado a Objetos.

Foro tecnico para desarrolladores con arquitectura de microservicios en Spring Boot, base de datos PostgreSQL y frontend estatico desplegado en produccion.

**Aplicacion en produccion:** https://foro-developers-soft.netlify.app/login.html

---

## Indice

1. [La aplicacion](#la-aplicacion)
2. [Stack tecnico](#stack-tecnico)
3. [Como usar la web](#como-usar-la-web)
4. [Arquitectura del sistema](#arquitectura-del-sistema)
5. [Microservicios y fakes](#microservicios-y-fakes)
6. [Casos de uso implementados](#casos-de-uso-implementados)
7. [Estructura del proyecto](#estructura-del-proyecto)
8. [Modelo de datos](#modelo-de-datos)
9. [API - Referencia de endpoints](#api---referencia-de-endpoints)
10. [Despliegue](#despliegue)
11. [Ejecucion en local](#ejecucion-en-local)
12. [Trazabilidad diagrama - codigo](#trazabilidad-diagrama---codigo)

---

## La aplicacion

La aplicacion esta disponible en produccion en la siguiente URL:

**https://foro-developers-soft.netlify.app/login.html**

Es un foro de preguntas y respuestas orientado a desarrolladores software, al estilo de Stack Overflow pero simplificado para cubrir los cuatro casos de uso del proyecto. Los usuarios pueden publicar preguntas tecnicas, responderlas, votar las respuestas de otros, reportar contenido inapropiado y marcar una respuesta como la solucion definitiva a su pregunta.

No hay registro de usuarios como tal: el sistema usa un identificador numerico simple almacenado en el navegador. Para empezar a usar la aplicacion basta con introducir cualquier numero entero positivo como ID. Esto es un sistema de autenticacion fake, suficiente para el alcance del proyecto, que permite probar todos los casos de uso con distintos usuarios simplemente cambiando de ID.

---

## Stack tecnico

**Backend:**
- Java 21
- Spring Boot 3.2.5
- Spring Data JPA con Hibernate
- PostgreSQL 15 como base de datos
- HikariCP como pool de conexiones
- Maven como gestor de dependencias

**Frontend:**
- HTML5, CSS3 y JavaScript vanilla (sin frameworks)
- Sin proceso de build, archivos estaticos directamente desplegables

**Infraestructura:**
- Railway para el backend (dos servicios independientes, cada uno con su propia base de datos PostgreSQL)
- Netlify para el frontend (sitio estatico)

El backend esta implementado como dos microservicios Spring Boot independientes que se comunican entre si por HTTP. Cada microservicio tiene su propia base de datos PostgreSQL en Railway y expone su propia API REST.

---

## Como usar la web

Al entrar a https://foro-developers-soft.netlify.app/login.html aparece una pantalla de login minimalista con un campo para introducir el ID de usuario. Basta con escribir cualquier numero (por ejemplo, 1, 2, 3...) y pulsar entrar.

### Pagina principal

Tras identificarse, la pagina principal muestra el listado de todas las preguntas publicadas. Cada pregunta aparece como una tarjeta con el titulo, un extracto del contenido, las etiquetas tecnicas asociadas (Java, Spring Boot, SQL, etc.), el numero de respuestas y el usuario y fecha de publicacion. Si la pregunta ya tiene una respuesta aceptada aparece un distintivo verde con una marca de verificacion.

Desde aqui se puede pulsar en cualquier pregunta para ver su detalle, o usar el boton "+ Nueva pregunta" en la esquina superior derecha para publicar una pregunta propia.

### Detalle de una pregunta

En la pagina de detalle se muestra el contenido completo de la pregunta, sus etiquetas y un pie con el boton de reportar y, si el usuario logueado es el autor, el boton de eliminar.

Debajo aparecen todas las respuestas publicadas. Cada respuesta tiene:
- Botones de voto positivo (triangulo arriba) y negativo (triangulo abajo) con el score actual en medio
- El contenido de la respuesta
- El boton de reportar
- Si el usuario logueado es el autor de la pregunta y aun no hay respuesta aceptada: el boton "Aceptar"
- Si hay una respuesta aceptada y el usuario es el autor de la pregunta: el boton "Quitar aceptada"
- Si el usuario logueado es el autor de la respuesta o el autor de la pregunta: el boton "Eliminar"
- El ID del autor de la respuesta

Al final de la pagina hay un formulario de texto para publicar una nueva respuesta.

### Nueva pregunta

El formulario de nueva pregunta tiene tres campos: titulo (obligatorio), cuerpo de la pregunta (obligatorio) y un selector de etiquetas con las ocho categorias disponibles. Tras publicar, la pagina redirige automaticamente al detalle de la pregunta recien creada.

### Navegar entre usuarios

Para probar el sistema con distintos usuarios basta con pulsar "Salir" en el encabezado y volver a entrar con un ID diferente. Esto permite, por ejemplo, publicar una pregunta con el usuario 1, responderla con el usuario 2, y votar la respuesta o reportarla con el usuario 3.

---

## Arquitectura del sistema

El sistema sigue una arquitectura de microservicios con los patrones CQRS y Event-Driven Architecture, tal como establece la especificacion del proyecto. Se implementan completamente dos microservicios; el resto de servicios del ecosistema (busqueda, notificaciones, reputacion, etc.) se simulan mediante clases fake.

```
+----------------------------------------------+
|  Frontend estatico (Netlify)                 |
|  HTML + CSS + JavaScript vanilla             |
+---------------+--------------+---------------+
                |              |
           HTTP REST      HTTP REST
                |              |
                v              v
  +--------------------+  +--------------------+
  |  servicio-         |  |  servicio-         |
  |  publicaciones     |  |  votaciones        |
  |  Spring Boot :8081 |  |  Spring Boot :8082 |
  |  PostgreSQL propio |  |  PostgreSQL propio |
  +--------------------+  +--------------------+
         |    ^                    |
         |    | PATCH /score       |
         |    +--------------------+
         |
         v
  [FakeMessageBroker]
  Simula el Message Broker (Kafka en el sistema real)
  invocando a los consumidores de forma sincrona
```

El frontend se comunica directamente con los dos microservicios sin API Gateway (no implementado en esta entrega). Las URLs de cada servicio se configuran en `frontend/js/config.js`.

La unica comunicacion real entre microservicios ocurre en CU1 (votacion): cuando se registra un voto, `servicio-votaciones` hace un `PATCH /publicaciones/{id}/score` a `servicio-publicaciones` para actualizar el score almacenado. Si esa llamada falla (por ejemplo, porque `PUBLICACIONES_URL` no esta configurada), el sistema no cae: los votos se siguen guardando correctamente y el score se calcula en tiempo real sumando los votos de la base de datos de votaciones.

---

## Microservicios y fakes

### Servicios implementados

| Microservicio | Tecnologia | Puerto local |
|---|---|---|
| servicio-publicaciones | Spring Boot 3.2.5 + PostgreSQL | 8081 |
| servicio-votaciones | Spring Boot 3.2.5 + PostgreSQL | 8082 |

### Servicios simulados con fakes

El sistema real requeriria varios microservicios adicionales (busqueda en Elasticsearch, notificaciones, reputacion de usuarios, etc.) y un Message Broker como Kafka. Para el alcance de este proyecto esos servicios se simulan con clases Java sencillas anotadas con `@Component` que tienen los mismos metodos que tendria el cliente real, pero en lugar de hacer llamadas de red imprimen una traza por consola.

Spring los inyecta exactamente igual que cualquier bean real, asi que el codigo de produccion no sabe si esta hablando con un servicio real o con un fake. Esto permite cambiar cualquier fake por una implementacion real en el futuro sin tocar el codigo que lo usa.

```java
// Ejemplo de fake: mismo contrato que el servicio real, cero dependencias de red
@Component
public class FakeServicioBusqueda {
    public void indexarEnElasticsearch(Long preguntaId) {
        System.out.printf("[FakeServicioBusqueda] indexar id=%d%n", preguntaId);
    }
    public void eliminarDelIndice(Long publicacionId) {
        System.out.printf("[FakeServicioBusqueda] eliminar id=%d%n", publicacionId);
    }
}
```

### El FakeMessageBroker

En el sistema real los servicios publicarian eventos en Kafka y los consumidores los recibirian de forma asincrona. El `FakeMessageBroker` reproduce ese comportamiento de forma sincrona: cuando alguien llama a `publish(evento, id)`, el broker invoca inmediatamente a los consumidores que le corresponden segun el evento.

Hay un `FakeMessageBroker` en cada microservicio, con sus propios consumidores:

| Evento | Broker en | Consumidores invocados |
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

Flujo normal: el usuario pulsa el triangulo de subir o bajar sobre una respuesta. El frontend envia `POST /votos` con `usuarioId`, `respuestaId`, `valor` (+1 o -1) y `autorRespuestaId` (el ID del autor de esa respuesta, que el frontend ya conoce porque lo renderiza). El servicio comprueba que el usuario no sea el autor, consulta si ya habia votado antes y actua en consecuencia:

- Si no habia votado: registra el voto y publica `VotoEmitido`
- Si habia votado igual: retira el voto y publica `VotoRetirado`
- Si habia votado al contrario: sustituye el voto en operacion atomica y publica `VotoCambiado`

Despues intenta actualizar el score en servicio-publicaciones via PATCH. Si ese servicio no esta disponible, calcula el score sumando los votos en su propia base de datos y lo devuelve igual. El frontend actualiza el numero en pantalla sin recargar la pagina.

Al cargar el detalle de cualquier pregunta, los scores se obtienen siempre de `GET /votos/scores?ids=...` (servicio-votaciones), que es la fuente de verdad real.

Validaciones:
- Autovoto: 403. La comprobacion usa `autorRespuestaId` del body para no depender de una llamada inter-servicio
- Valor distinto de +1 o -1: 400

### CU2 - Reportar publicacion para moderacion

Flujo normal: el usuario pulsa "Reportar" en una pregunta o respuesta, introduce el motivo y confirma. El backend verifica que la publicacion existe y esta en estado VISIBLE, que el usuario no es su autor y que no ha reportado ya esa publicacion. Registra el reporte y cuenta el total acumulado.

Si el total no alcanza el limite (3 reportes), el sistema confirma la accion y el frontend muestra un aviso indicando cuantos reportes quedan para el umbral.

Si el total iguala o supera el limite, la publicacion pasa a estado OCULTA de forma automatica, se publica el evento `publicacion_ocultada` (que llama a FakeServicioBusqueda para eliminarla del indice y a FakeServicioNotificaciones para avisar a los administradores) y el frontend recarga la pagina, haciendo que la publicacion desaparezca.

Validaciones:
- Publicacion en estado ELIMINADA u OCULTA: 404 (no se puede reportar lo que no existe o ya esta bajo revision)
- Autorreporte: 403
- Reporte duplicado del mismo usuario sobre la misma publicacion: 409

### CU3 - Publicar una pregunta

Flujo normal: el usuario rellena el formulario con titulo (obligatorio), contenido (obligatorio) y una o varias etiquetas. El frontend valida los campos antes de enviar. El backend valida las etiquetas con FakeServicioEtiquetas, persiste la pregunta en PostgreSQL y emite `pregunta_publicada`, que FakeProyectorCQRS y FakeServicioBusqueda consumen para actualizar el modelo de lectura e indexar la pregunta respectivamente. El backend devuelve 201 y el frontend redirige al detalle de la pregunta recien creada.

Etiquetas disponibles: Java, Spring Boot, SQL, JPA, REST, Docker, Testing, Frontend.

### CU4 - Marcar respuesta como aceptada

Flujo normal: el autor de la pregunta ve un boton "Aceptar" en cada respuesta mientras no haya ninguna aceptada. Al pulsar, el backend verifica que el solicitante es efectivamente el autor de la pregunta y que no habia aceptada ya. Marca la respuesta como `esAceptada = true`, actualiza `acceptedAnswerId` en la pregunta y emite `respuesta_aceptada`, que suma puntos de reputacion al autor de la respuesta, actualiza el modelo de lectura y le envia una notificacion.

En el detalle, la respuesta aceptada aparece con un fondo verde y la etiqueta "Respuesta aceptada" en la parte superior.

El autor de la pregunta puede deshacer la aceptacion pulsando "Quitar aceptada" sobre la respuesta marcada. Esto permite aceptar otra respuesta diferente si cambia de opinion.

Validaciones:
- No es el autor de la pregunta: 403
- Ya existe una respuesta aceptada: 409 con mensaje que indica que hay que quitarla primero

---

## Estructura del proyecto

```
ingsoft_entregaDOO/
+-- backend/
|   +-- servicio-publicaciones/
|   |   +-- src/main/java/com/grupok/publicaciones/
|   |       +-- controller/
|   |       |   +-- PreguntaController.java         GET+POST /preguntas, DELETE /preguntas/{id}
|   |       |   +-- RespuestaController.java        POST+DELETE /respuestas, PATCH /aceptar, PATCH /desaceptar
|   |       |   +-- ReporteController.java          POST /publicaciones/{id}/reportes
|   |       |   +-- PublicacionController.java      PATCH /publicaciones/{id}/score
|   |       |   +-- GlobalExceptionHandler.java     errores uniformes como {"error":"..."}
|   |       +-- service/
|   |       |   +-- PreguntaService.java
|   |       |   +-- RespuestaService.java
|   |       |   +-- ReporteService.java
|   |       |   +-- PublicacionService.java
|   |       +-- repository/
|   |       |   +-- PreguntaRepository.java
|   |       |   +-- PublicacionRepository.java      query custom findPreguntaByRespuestaId
|   |       |   +-- RespuestaRepository.java        countVisibleByPreguntaId (excluye OCULTA y ELIMINADA)
|   |       |   +-- ReporteRepository.java          countByPublicacionId, existsByUsuarioIdAndPublicacionId
|   |       +-- model/
|   |       |   +-- Publicacion.java                entidad base con InheritanceType.JOINED
|   |       |   +-- Pregunta.java
|   |       |   +-- Respuesta.java
|   |       |   +-- Reporte.java
|   |       |   +-- EstadoPublicacion.java          enum VISIBLE / OCULTA / ELIMINADA
|   |       +-- dto/
|   |       |   +-- PublicarPreguntaRequest.java
|   |       |   +-- PublicarRespuestaRequest.java
|   |       |   +-- AceptarRespuestaRequest.java
|   |       |   +-- ReportarPublicacionRequest.java
|   |       |   +-- ReporteResultadoDto.java        numReportes, reportesRestantes, publicacionOculta
|   |       |   +-- PreguntaResumenDto.java
|   |       |   +-- PreguntaDetalleDto.java
|   |       +-- fake/
|   |       |   +-- FakeMessageBroker.java
|   |       |   +-- FakeServicioEtiquetas.java
|   |       |   +-- FakeServicioUsuarios.java
|   |       |   +-- FakeServicioNotificaciones.java   notificarAutorRespuesta + notificarAdministradores
|   |       |   +-- FakeServicioBusqueda.java         indexarEnElasticsearch + eliminarDelIndice
|   |       |   +-- FakeProyectorCQRS.java
|   |       +-- config/
|   |       |   +-- CorsConfig.java
|   |       +-- DataSourceConfig.java               HikariCP con soporte para DATABASE_URL de Railway
|   |
|   +-- servicio-votaciones/
|       +-- src/main/java/com/grupok/votaciones/
|           +-- controller/
|           |   +-- VotoController.java             POST /votos, GET /votos/scores
|           |   +-- GlobalExceptionHandler.java
|           +-- service/
|           |   +-- VotoService.java                emitirVoto, calcularScores
|           +-- repository/
|           |   +-- VotoRepository.java
|           +-- model/
|           |   +-- Voto.java
|           +-- dto/
|           |   +-- EmitirVotoRequest.java          incluye autorRespuestaId para verificar autovoto sin llamada inter-servicio
|           +-- config/
|           |   +-- CorsConfig.java
|           |   +-- RestTemplateConfig.java         RestTemplate con HttpComponentsClientHttpRequestFactory (necesario para PATCH)
|           +-- fake/
|               +-- FakeMessageBroker.java
|               +-- FakeServicioReputacion.java
|               +-- FakeServicioNotificaciones.java
|
+-- frontend/
    +-- index.html                                  Listado de preguntas
    +-- pregunta.html                               Detalle de pregunta y respuestas
    +-- nueva-pregunta.html                         Formulario nueva pregunta
    +-- login.html                                  Identificacion
    +-- css/
    |   +-- styles.css
    +-- js/
        +-- config.js                               URLs de los microservicios
        +-- api.js                                  Helper HTTP, auth, toasts, escape HTML
        +-- index.js
        +-- pregunta.js
        +-- nueva-pregunta.js
```

---

## Modelo de datos

### servicio-publicaciones

La herencia entre `Pregunta` y `Respuesta` se implementa con `InheritanceType.JOINED` de JPA: existe una tabla `publicacion` con los campos comunes y tablas separadas `pregunta` y `respuesta` con sus campos especificos.

```
publicacion  (tabla base)
  id              BIGINT      PK, autogenerado
  autor_id        BIGINT
  score           INT         actualizado por PATCH desde servicio-votaciones
  estado          VARCHAR     VISIBLE | OCULTA | ELIMINADA  (default VISIBLE)
  fecha_creacion  TIMESTAMP

pregunta  (extiende publicacion)
  titulo              VARCHAR
  contenido           TEXT
  accepted_answer_id  BIGINT   null mientras no haya respuesta aceptada

pregunta_etiqueta_ids  (coleccion de pregunta)
  pregunta_id   BIGINT   FK -> pregunta
  etiqueta_ids  BIGINT

respuesta  (extiende publicacion)
  contenido    TEXT
  es_aceptada  BOOLEAN   false por defecto
  pregunta_id  BIGINT    FK -> pregunta

reporte  (tabla independiente)
  id              BIGINT   PK, autogenerado
  publicacion_id  BIGINT
  usuario_id      BIGINT
  motivo          VARCHAR
```

**Estados de publicacion:**

| Estado | Descripcion |
|---|---|
| VISIBLE | Publicacion normal, accesible y visible para todos |
| OCULTA | Ocultada automaticamente al superar el limite de reportes. No aparece en listados ni en detalle. No admite nuevos reportes ni nuevas respuestas. Si se accede por URL directa devuelve 403. |
| ELIMINADA | Borrada por su autor mediante soft delete. No se muestra ni se puede acceder. Devuelve 404. |

### servicio-votaciones

```
voto
  id            BIGINT   PK, autogenerado
  usuario_id    BIGINT
  respuesta_id  BIGINT
  valor         INT      +1 o -1
```

Restriccion de negocio: un usuario solo puede tener un voto activo por respuesta. El par `(usuario_id, respuesta_id)` es efectivamente unico en cualquier momento dado.

---

## API - Referencia de endpoints

### servicio-publicaciones (puerto 8081)

| Metodo | Ruta | Descripcion | CU |
|---|---|---|---|
| GET | `/preguntas` | Lista todas las preguntas visibles con resumen | CU3 |
| POST | `/preguntas` | Publica una nueva pregunta | CU3 |
| GET | `/preguntas/{id}` | Detalle de una pregunta con sus respuestas visibles | CU3/CU4 |
| DELETE | `/preguntas/{id}?usuarioId=X` | Elimina la pregunta (solo el autor) | - |
| POST | `/respuestas` | Publica una respuesta a una pregunta | - |
| PATCH | `/respuestas/{id}/aceptar` | Marca la respuesta como aceptada | CU4 |
| PATCH | `/respuestas/{id}/desaceptar` | Retira la aceptacion de una respuesta | CU4 ext. |
| DELETE | `/respuestas/{id}?usuarioId=X` | Elimina la respuesta (autor o autor de la pregunta) | - |
| POST | `/publicaciones/{id}/reportes` | Reporta una publicacion | CU2 |
| PATCH | `/publicaciones/{id}/score` | Actualiza el score (llamado por servicio-votaciones) | CU1 |

#### POST /preguntas

```json
// Request
{
  "usuarioId": 1,
  "titulo": "Como configurar JPA con herencia JOINED en Spring Boot?",
  "contenido": "Tengo dos entidades que heredan de una base...",
  "etiquetaIds": [2, 4]
}

// Response 201
{
  "id": 42,
  "autorId": 1,
  "score": 0,
  "estado": "VISIBLE",
  "titulo": "Como configurar JPA con herencia JOINED en Spring Boot?",
  "contenido": "Tengo dos entidades que heredan de una base...",
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
    "acceptedAnswerId": 7,
    "fechaCreacion": "2026-05-24T10:30:00",
    "estado": "VISIBLE"
  },
  "respuestas": [
    {
      "id": 7,
      "autorId": 3,
      "score": 2,
      "contenido": "...",
      "esAceptada": true,
      "preguntaId": 42,
      "estado": "VISIBLE"
    }
  ]
}
```

#### POST /publicaciones/{id}/reportes

```json
// Request
{ "usuarioId": 2, "motivo": "Contenido ofensivo" }

// Response 200
{
  "numReportes": 2,
  "reportesRestantes": 1,
  "publicacionOculta": false
}
```

Cuando `publicacionOculta` es `true`, la publicacion ha sido ocultada automaticamente.

### servicio-votaciones (puerto 8082)

| Metodo | Ruta | Descripcion | CU |
|---|---|---|---|
| POST | `/votos` | Emite un voto sobre una respuesta | CU1 |
| GET | `/votos/scores?ids=1,2,3` | Devuelve los scores actuales de varias respuestas | CU1 |

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

Comportamiento segun el estado previo del voto del usuario sobre esa respuesta:

| Situacion | Accion | Evento publicado |
|---|---|---|
| Sin voto previo | Se registra el voto | VotoEmitido |
| Mismo tipo de voto ya registrado | Se retira el voto | VotoRetirado |
| Tipo de voto contrario ya registrado | Se sustituye en una sola operacion | VotoCambiado |

#### GET /votos/scores?ids=1,2,3

```json
// Response 200
{
  "7": 3,
  "8": -1,
  "9": 0
}
```

### Codigos de error

Todos los errores devuelven la misma estructura:

```json
{ "error": "Descripcion legible del error" }
```

| HTTP | Cuando ocurre |
|---|---|
| 400 | Campo obligatorio ausente o valor invalido (por ejemplo, valor de voto distinto de +1 o -1) |
| 403 | Operacion no permitida: autovoto, autorreporte, o accion reservada al autor |
| 404 | La entidad no existe, ha sido eliminada o esta en estado OCULTA |
| 409 | Conflicto de estado: reporte duplicado, pregunta ya tiene respuesta aceptada |

---

## Despliegue

### Backend en Railway

Ambos microservicios estan desplegados como servicios independientes en Railway, cada uno con su propio plugin de PostgreSQL. Railway gestiona automaticamente el ciclo de vida de los contenedores y las variables de entorno de la base de datos.

El proceso de despliegue es el siguiente: Railway detecta el `pom.xml` en la raiz del servicio, compila el proyecto con Maven y ejecuta el JAR resultante. El puerto lo gestiona Railway mediante la variable `PORT`, que Spring Boot lee con `server.port=${PORT:8081}`.

**Variables de entorno en servicio-publicaciones:**

| Variable | Descripcion |
|---|---|
| `DATABASE_URL` | URL completa de PostgreSQL en formato `postgresql://user:pass@host:port/db`. Railway la inyecta automaticamente al conectar el plugin de base de datos. |
| `PORT` | Inyectada automaticamente por Railway. |

Railway proporciona la URL con credenciales inlineadas en el formato `postgresql://user:pass@host/db`. El driver JDBC de PostgreSQL no acepta este formato directamente. Para resolverlo, `DataSourceConfig.java` descompone la URL con `java.net.URI` y configura usuario y contrasena por separado en HikariCP, construyendo una URL JDBC limpia del tipo `jdbc:postgresql://host:port/db`.

**Variables de entorno en servicio-votaciones:**

Railway inyecta automaticamente `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` y `PGPASSWORD` al conectar el plugin de PostgreSQL, y el `application.properties` las lee directamente. La unica variable que hay que configurar manualmente es:

| Variable | Descripcion |
|---|---|
| `PUBLICACIONES_URL` | URL publica de servicio-publicaciones en Railway (por ejemplo, `https://publicaciones-production.up.railway.app`). Necesaria para que CU1 actualice el score en la base de datos de publicaciones. |

Si `PUBLICACIONES_URL` no esta configurada, el PATCH de actualizacion de score fallara silenciosamente. Los votos se siguen guardando y el score se calcula en tiempo real desde la base de datos de votaciones, por lo que la aplicacion funciona correctamente de cara al usuario.

### Frontend en Netlify

El frontend es un conjunto de archivos estaticos que no requiere proceso de build. El despliegue en Netlify consiste en subir el contenido de la carpeta `frontend/` y configurar la carpeta raiz del proyecto como directorio de publicacion.

Antes de subir hay que asegurarse de que `frontend/js/config.js` contiene las URLs de produccion de Railway:

```javascript
window.CONF = {
  API_PUB: 'https://publicaciones-production.up.railway.app',
  API_VOT: 'https://votaciones-production.up.railway.app'
};
```

Netlify sirve los archivos directamente sin configuracion adicional. El dominio asignado al proyecto es `foro-developers-soft.netlify.app`.

---

## Ejecucion en local

### Prerrequisitos

- Java 21
- Maven 3.9 o superior
- PostgreSQL corriendo en `localhost:5432`

### Base de datos

```sql
CREATE DATABASE foro_publicaciones;
CREATE DATABASE foro_votaciones;
```

Hibernate crea las tablas automaticamente al arrancar cada servicio (`spring.jpa.hibernate.ddl-auto=update`). No hace falta ejecutar ningun script SQL.

### Arrancar los servicios

```bash
# Terminal 1
cd backend/servicio-publicaciones
mvn spring-boot:run
# API disponible en http://localhost:8081

# Terminal 2
cd backend/servicio-votaciones
mvn spring-boot:run
# API disponible en http://localhost:8082
```

Conviene arrancar `servicio-publicaciones` primero porque `servicio-votaciones` le hace llamadas HTTP al procesar votos. Si se arranca en orden inverso la aplicacion funciona igual, pero el score almacenado en publicaciones no se actualizara hasta que publicaciones este disponible.

### Frontend

Abrir `frontend/index.html` directamente en el navegador o servirlo con cualquier servidor HTTP estatico (por ejemplo, la extension Live Server de VS Code). Verificar que `config.js` apunta a `localhost` antes de abrir.

---

## Trazabilidad diagrama - codigo

Los nombres de clases, metodos y rutas en el codigo son los mismos que los de los participantes y mensajes en los diagramas de secuencia de la especificacion del proyecto.

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
| boundary "API de Notificaciones" | `FakeServicioNotificaciones` | `@Component` |
| boundary "API de Busqueda" | `FakeServicioBusqueda` | `@Component` |
| boundary "API de Reputacion" | `FakeServicioReputacion` | `@Component` |
