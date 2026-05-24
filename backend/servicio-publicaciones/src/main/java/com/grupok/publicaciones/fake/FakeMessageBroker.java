package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Simula el message broker invocando a los consumidores de forma sincrona
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
            // nueva pregunta publicada
            case "pregunta_publicada" -> {
                fakeProyectorCQRS.actualizarModeloLectura(evento, id);
                fakeServicioBusqueda.indexarEnElasticsearch(id);
            }
            // respuesta marcada como aceptada
            case "respuesta_aceptada" -> {
                fakeServicioUsuarios.sumarPuntosReputacion(id);
                fakeProyectorCQRS.actualizarModeloLectura(evento, id);
                fakeServicioNotificaciones.notificarAutorRespuesta(id);
            }
            // publicacion ocultada por acumulacion de reportes
            case "publicacion_ocultada" -> {
                fakeServicioBusqueda.eliminarDelIndice(id);
                fakeServicioNotificaciones.notificarAdministradores(id);
                fakeProyectorCQRS.actualizarModeloLectura(evento, id);
            }
        }
    }
}
