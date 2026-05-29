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
5. [Casos de uso implementados](#casos-de-uso-implementados)
6. [Trazabilidad diagrama - codigo](#trazabilidad-diagrama---codigo)
7. [Pruebas y cobertura](#pruebas-y-cobertura)
8. [Estructura del proyecto](#estructura-del-proyecto)
9. [Despliegue](#despliegue)
10. [Ejecucion en local](#ejecucion-en-local)

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

Al final de la pagina hay un formulario de texto para publicar una nueva respuesta.

### Nueva pregunta

El formulario de nueva pregunta tiene tres campos: titulo (obligatorio), cuerpo de la pregunta (obligatorio) y un selector de etiquetas con las ocho categorias disponibles. Tras publicar, la pagina redirige automaticamente al detalle de la pregunta recien creada.

### Navegar entre usuarios

Para probar el sistema con distintos usuarios basta con pulsar "Salir" en el encabezado y volver a entrar con un ID diferente. Esto permite, por ejemplo, publicar una pregunta con el usuario 1, responderla con el usuario 2, y votar la respuesta o reportarla con el usuario 3.

---

## Casos de uso implementados

**CU1 - Votar una respuesta:** cualquier usuario puede dar +1 o -1 a la respuesta de otro. Si ya habia votado igual, se retira el voto. Si habia votado al contrario, se sustituye. No se puede votar la propia respuesta. Los scores se calculan siempre desde la base de datos de votaciones, que es la fuente de verdad.

**CU2 - Reportar contenido:** cualquier usuario puede reportar una pregunta o respuesta. Al llegar a 3 reportes de usuarios distintos, la publicacion se oculta automaticamente y desaparece de la web. No se puede reportar dos veces lo mismo ni reportar las propias publicaciones.

**CU3 - Publicar una pregunta:** formulario con titulo, contenido y etiquetas. Tras publicar redirige al detalle de la pregunta recien creada. Etiquetas disponibles: Java, Spring Boot, SQL, JPA, REST, Docker, Testing, Frontend.

**CU4 - Aceptar una respuesta:** el autor de la pregunta puede marcar una respuesta como solucion definitiva. Puede cambiarlo despues pulsando "Quitar aceptada" y aceptando otra diferente.

---

## Trazabilidad diagrama - codigo

Los nombres de clases, metodos y rutas en el codigo son exactamente los mismos que los que aparecen en los diagramas de secuencia entregados. Si en el diagrama aparece el participante `:VotoService`, en el codigo existe la clase `VotoService.java` con ese mismo nombre. Si el diagrama muestra el mensaje `reportarPublicacion()`, ese es el nombre exacto del metodo en `ReporteService.java`. El objetivo es que seguir un caso de uso entre el diagrama y el codigo no requiera ninguna traduccion: se puede ir directamente de un participante del diagrama al fichero correspondiente.

Esto aplica a todos los participantes: controladores (`@RestController`), servicios (`@Service`), repositorios (`@Repository`) y los servicios externos simulados (`@Component`), que en los diagramas aparecen como boundaries o queues y en el codigo son las clases `FakeMessageBroker`, `FakeServicioEtiquetas`, `FakeServicioBusqueda`, etc.

---

## Pruebas y cobertura

Las pruebas cubren los cuatro objetivos indicados en la tarea de testing del Grupo K:

| Objetivo | Clase de test | Servicio |
|---|---|---|
| `@PostMapping("/preguntas")` | `PreguntaIntegracionTest` | servicio-publicaciones |
| `PreguntaService.obtenerDetalle()` | `PreguntaServiceTest` | servicio-publicaciones |
| `ReporteService.reportarPublicacion()` | `ReporteServiceTest` | servicio-publicaciones |
| `@PostMapping("/votos")` | `VotoIntegracionTest` | servicio-votaciones |

### Ejecutar las pruebas

Requisitos: Java 21 y Maven. **No se necesita base de datos** - los tests de unidad usan Mockito y los de integracion usan MockMvc, todo en memoria.

```bash
cd backend/servicio-publicaciones
mvn test
```

```bash
cd backend/servicio-votaciones
mvn test
```

### Ver el informe de cobertura (JaCoCo)

La carpeta `target/` no esta en el repositorio (la excluye `.gitignore`). Hay que ejecutar `mvn test` primero para generarla.

Una vez ejecutado, abrir el informe haciendo **doble clic** en el archivo desde el explorador de archivos del sistema operativo:

```
backend/servicio-publicaciones/target/site/jacoco/index.html
backend/servicio-votaciones/target/site/jacoco/index.html
```

Alternativamente, desde el navegador: `Ctrl+O` y navegar hasta el archivo.

El informe muestra la cobertura de instrucciones, ramas y lineas por paquete y clase. JaCoCo esta configurado en el `pom.xml` de cada servicio y se genera automaticamente al lanzar `mvn test`.

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

## Despliegue

El backend esta en Railway con dos servicios independientes, cada uno con su propio plugin de PostgreSQL. Railway detecta el `pom.xml`, compila con Maven y ejecuta el JAR. El frontend esta en Netlify como sitio estatico: se sube la carpeta `frontend/` y se apuntan las URLs de Railway en `config.js`. Sin mas configuracion.

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
