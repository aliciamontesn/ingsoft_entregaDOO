package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Message Broker — CU3 y CU4: publish(evento)
// Invoca síncronamente a los consumidores fake simulando la entrega asíncrona del broker
@Component
public class FakeMessageBroker {

    private final FakeServicioBusqueda fakeServicioBusqueda;
    private final FakeServicioUsuarios fakeServicioUsuarios;
    private final FakeServicioNotificaciones fakeServicioNotificaciones;
    private final FakeProyectorCQRS fakeProyectorCQRS;

    public FakeMessageBroker(FakeServicioBusqueda fakeServicioBusqueda,
                             FakeServicioUsuarios fakeServicioUsuarios,
                             FakeServicioNotificaciones fakeServicioNotificaciones,
                             FakeProyectorCQRS fakeProyectorCQRS) {
        this.fakeServicioBusqueda = fakeServicioBusqueda;
        this.fakeServicioUsuarios = fakeServicioUsuarios;
        this.fakeServicioNotificaciones = fakeServicioNotificaciones;
        this.fakeProyectorCQRS = fakeProyectorCQRS;
    }

    public void publish(String evento, Long id) {
        System.out.printf("[FakeMessageBroker] publish evento='%s' id=%d%n", evento, id);
        switch (evento) {
            // CU3: consumidores de pregunta_publicada
            case "pregunta_publicada" -> {
                fakeProyectorCQRS.actualizarModeloLectura(evento, id);
                fakeServicioBusqueda.indexarEnElasticsearch(id);
            }
            // CU4: consumidores de respuesta_aceptada
            case "respuesta_aceptada" -> {
                fakeServicioUsuarios.sumarPuntosReputacion(id);
                fakeProyectorCQRS.actualizarModeloLectura(evento, id);
                fakeServicioNotificaciones.notificarAutorRespuesta(id);
            }
            // CU2 ext.6a: publicación auto-ocultada por exceso de reportes
            case "publicacion_ocultada" -> {
                fakeServicioBusqueda.eliminarDelIndice(id);
                fakeServicioNotificaciones.notificarAdministradores(id);
                fakeProyectorCQRS.actualizarModeloLectura(evento, id);
            }
        }
    }
}
