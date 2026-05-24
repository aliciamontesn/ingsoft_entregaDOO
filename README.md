# Foro de Desarrolladores - Grupo K

Trabajo Ingenieria del Software - Disenio Orientado a Objetos.

Foro tecnico para desarrolladores con arquitectura de microservicios en Spring Boot, base de datos PostgreSQL y frontend estatico desplegado en produccion.

**Aplicacion en produccion:** https://foro-developers-soft.netlify.app/login.html

---

## Indice

1. [La aplicacion](#la-aplicacion)
2. [Datos de ejemplo cargados](#datos-de-ejemplo-cargados)
3. [Stack tecnico](#stack-tecnico)
4. [Como usar la web](#como-usar-la-web)
5. [Arquitectura del sistema](#arquitectura-del-sistema)
6. [Microservicios y fakes](#microservicios-y-fakes)
7. [Casos de uso implementados](#casos-de-uso-implementados)
8. [Estructura del proyecto](#estructura-del-proyecto)
9. [Modelo de datos](#modelo-de-datos)
10. [API](#api)
11. [Despliegue](#despliegue)
12. [Ejecucion en local](#ejecucion-en-local)
13. [Trazabilidad diagrama - codigo](#trazabilidad-diagrama---codigo)

---

## La aplicacion

La aplicacion esta disponible en produccion en la siguiente URL:

**https://foro-developers-soft.netlify.app/login.html**

Es un foro de preguntas y respuestas orientado a desarrolladores software, al estilo de Stack Overflow pero simplificado para cubrir los cuatro casos de uso del proyecto. Los usuarios pueden publicar preguntas tecnicas, responderlas, votar las respuestas de otros, reportar contenido inapropiado y marcar una respuesta como la solucion definitiva a su pregunta.

No hay registro de usuarios como tal: el sistema usa un identificador numerico simple almacenado en el navegador. Para empezar a usar la aplicacion basta con introducir cualquier numero entero positivo como ID. Esto es un sistema de autenticacion fake, suficiente para el alcance del proyecto, que permite probar todos los casos de uso con distintos usuarios simplemente cambiando de ID.

---

## Datos de ejemplo cargados

La aplicacion tiene datos de ejemplo cargados en produccion para que se pueda ver el funcionamiento real sin necesidad de crear contenido desde cero.

Las preguntas y respuestas estan inspiradas en dudas tecnicas reales extraidas de Stack Overflow, el foro de referencia para desarrolladores. Se han adaptado ligeramente para que encajen con las etiquetas y el contexto del proyecto, pero el contenido tecnico es representativo de lo que se encuentra habitualmente en ese tipo de comunidades.

### Escenario de ejemplo

Se han creado cuatro usuarios (IDs 1, 2, 3 y 4) que interactuan entre si cubriendo todos los casos de uso:

**Pregunta 1 - usuario 1** (Java, Spring Boot): duda sobre NullPointerException al inyectar un @Service dentro de una clase @Configuration.
- Usuario 2 responde con la solucion correcta (inyeccion por parametro en el metodo @Bean).
- Usuario 3 responde con una alternativa usando @Lazy.
- Usuarios 3 y 4 votan positivo la respuesta de usuario 2.
- Usuario 1 vota negativo la respuesta de usuario 3.
- Usuario 1 acepta la respuesta de usuario 2 como solucion definitiva.

**Pregunta 2 - usuario 1** (SQL, PostgreSQL): duda sobre un LEFT JOIN lento en una tabla con millones de registros y el planificador de consultas ignorando los indices.
- Usuario 2 responde explicando como actualizar estadisticas con ANALYZE y ajustar random_page_cost.
- Usuario 4 vota positivo la respuesta de usuario 2.

### Sistema de reportes

Cualquier usuario puede reportar una publicacion que considere inapropiada o de baja calidad. Cuando una publicacion acumula 3 reportes de usuarios distintos, el sistema la oculta automaticamente: deja de aparecer en el listado y en el detalle de la pregunta, y se publica un evento interno que notifica a los administradores. Un usuario no puede reportar la misma publicacion dos veces ni reportar sus propias publicaciones.

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

El proyecto tiene dos microservicios reales (`servicio-publicaciones` en el puerto 8081 y `servicio-votaciones` en el 8082), cada uno con su propia base de datos PostgreSQL. El resto de servicios que existirian en el sistema completo (busqueda, notificaciones, reputacion de usuarios, un message broker tipo Kafka...) estan simulados con clases Java simples anotadas con `@Component`. Estas clases tienen la misma interfaz que tendrian los servicios reales pero en lugar de hacer llamadas de red simplemente imprimen una traza por consola. Spring los inyecta igual que cualquier otro bean, por lo que cambiarlos por implementaciones reales en el futuro no requeriria tocar el codigo de negocio.

---

## Casos de uso implementados

**CU1 - Votar una respuesta:** cualquier usuario puede dar +1 o -1 a la respuesta de otro. Si ya habia votado igual, se retira el voto. Si habia votado al contrario, se sustituye. No se puede votar la propia respuesta. Los scores se calculan siempre desde la base de datos de votaciones, que es la fuente de verdad.

**CU2 - Reportar contenido:** cualquier usuario puede reportar una pregunta o respuesta. Al llegar a 3 reportes de usuarios distintos, la publicacion se oculta automaticamente y desaparece de la web. No se puede reportar dos veces lo mismo ni reportar las propias publicaciones.

**CU3 - Publicar una pregunta:** formulario con titulo, contenido y etiquetas. Tras publicar redirige al detalle de la pregunta recien creada. Etiquetas disponibles: Java, Spring Boot, SQL, JPA, REST, Docker, Testing, Frontend.

**CU4 - Aceptar una respuesta:** el autor de la pregunta puede marcar una respuesta como solucion definitiva. Puede cambiarlo despues pulsando "Quitar aceptada" y aceptando otra diferente.

---

## Estructura del proyecto

```
ingsoft_entregaDOO/
+-- backend/
|   +-- servicio-publicaciones/    Spring Boot 3.2.5, puerto 8081
|   +-- servicio-votaciones/       Spring Boot 3.2.5, puerto 8082
+-- frontend/
    +-- index.html                 listado de preguntas
    +-- pregunta.html              detalle y respuestas
    +-- nueva-pregunta.html        formulario
    +-- login.html
    +-- js/
        +-- config.js              URLs de los microservicios
        +-- api.js                 helper HTTP y autenticacion
        +-- index.js / pregunta.js / nueva-pregunta.js
```

---

## Modelo de datos

Pregunta y Respuesta heredan de una entidad base `Publicacion` mediante `InheritanceType.JOINED`. El estado de cada publicacion puede ser `VISIBLE`, `OCULTA` (ocultada por reportes) o `ELIMINADA` (borrado logico por el autor). Los votos viven en una tabla separada en la base de datos de votaciones.

---

## API

**servicio-publicaciones (8081)**

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/preguntas` | lista de preguntas visibles |
| POST | `/preguntas` | nueva pregunta |
| GET | `/preguntas/{id}` | detalle con respuestas |
| DELETE | `/preguntas/{id}?usuarioId=X` | eliminar (solo el autor) |
| POST | `/respuestas` | nueva respuesta |
| PATCH | `/respuestas/{id}/aceptar` | aceptar respuesta |
| PATCH | `/respuestas/{id}/desaceptar` | quitar aceptacion |
| DELETE | `/respuestas/{id}?usuarioId=X` | eliminar respuesta |
| POST | `/publicaciones/{id}/reportes` | reportar |
| PATCH | `/publicaciones/{id}/score` | actualizar score (llamado internamente por votaciones) |

**servicio-votaciones (8082)**

| Metodo | Ruta | Descripcion |
|---|---|---|
| POST | `/votos` | emitir voto (+1 o -1) |
| GET | `/votos/scores?ids=1,2,3` | scores actuales de varias respuestas |

Todos los errores devuelven `{ "error": "..." }` con el codigo HTTP correspondiente (400, 403, 404 o 409).

---

## Despliegue

El backend esta en Railway: cada microservicio es un servicio independiente con su propio plugin de PostgreSQL. Railway detecta el `pom.xml`, compila con Maven y ejecuta el JAR. El puerto lo gestiona Railway automaticamente via la variable `PORT`.

Un detalle tecnico a tener en cuenta: Railway proporciona la URL de la base de datos en formato `postgresql://user:pass@host/db`, que el driver JDBC no acepta directamente. El fichero `DataSourceConfig.java` la parsea con `java.net.URI` y construye la URL JDBC correcta para HikariCP.

El frontend es un sitio estatico en Netlify. No requiere proceso de build: se sube la carpeta `frontend/` y se apunta `config.js` a las URLs de Railway. Listo.

---

## Ejecucion en local

Requisitos: Java 21, Maven y PostgreSQL en `localhost:5432`.

```sql
CREATE DATABASE foro_publicaciones;
CREATE DATABASE foro_votaciones;
```

```bash
cd backend/servicio-publicaciones && mvn spring-boot:run   # puerto 8081
cd backend/servicio-votaciones && mvn spring-boot:run      # puerto 8082
```

Hibernate crea las tablas solo al arrancar. El frontend se abre directamente en el navegador desde `frontend/index.html` (verificar que `config.js` apunta a localhost).

---

## Trazabilidad diagrama - codigo

Los nombres de clases y metodos en el codigo corresponden directamente con los participantes de los diagramas de secuencia de la especificacion: `VotoController`, `VotoService`, `VotoRepository` en votaciones; `PreguntaController`, `PreguntaService`, `ReporteController`, `ReporteService`, `RespuestaController`, `RespuestaService` en publicaciones. El `FakeMessageBroker` representa la cola de mensajes (Kafka en el sistema real) y los fakes representan cada uno de los microservicios externos.
