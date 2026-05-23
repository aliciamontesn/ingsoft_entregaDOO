package com.grupok.votaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Message Broker — CU1: publish(VotoEmitido)
// Invoca síncronamente a los consumidores fake simulando la entrega asíncrona del broker
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
            // CU1: voto nuevo
            case "VotoEmitido" -> {
                fakeServicioReputacion.actualizarReputacion(id, 0);
                fakeServicioNotificaciones.notificarAutorVoto(id);
            }
            // CU1 ext.4a: voto retirado
            case "VotoRetirado" -> fakeServicioReputacion.actualizarReputacion(id, 0);
            // CU1 ext.4b: voto cambiado
            case "VotoCambiado" -> fakeServicioReputacion.actualizarReputacion(id, 0);
        }
    }
}
