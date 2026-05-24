package com.grupok.votaciones.fake;

import org.springframework.stereotype.Component;

// Simula el message broker invocando a los consumidores de forma sincrona
@Component
public class FakeMessageBroker {

    private final FakeServicioReputacion fakeServicioReputacion;
    private final FakeServicioNotificaciones fakeServicioNotificaciones;

    public FakeMessageBroker(FakeServicioReputacion fakeServicioReputacion,
                             FakeServicioNotificaciones fakeServicioNotificaciones) {
        this.fakeServicioReputacion = fakeServicioReputacion;
        this.fakeServicioNotificaciones = fakeServicioNotificaciones;
    }

    public void publish(String evento, Long id) {
        System.out.printf("[FakeMessageBroker] publish evento='%s' id=%d%n", evento, id);
        switch (evento) {
            // voto nuevo
            case "VotoEmitido" -> {
                fakeServicioReputacion.actualizarReputacion(id, 0);
                fakeServicioNotificaciones.notificarAutorVoto(id);
            }
            // voto retirado
            case "VotoRetirado" -> fakeServicioReputacion.actualizarReputacion(id, 0);
            // voto cambiado de signo
            case "VotoCambiado" -> fakeServicioReputacion.actualizarReputacion(id, 0);
        }
    }
}
