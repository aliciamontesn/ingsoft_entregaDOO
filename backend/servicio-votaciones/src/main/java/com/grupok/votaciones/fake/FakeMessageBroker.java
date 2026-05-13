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
        // CU1: consumidores de VotoEmitido
        if ("VotoEmitido".equals(evento)) {
            fakeServicioReputacion.actualizarReputacion(id, 0);
            fakeServicioNotificaciones.notificarAutorVoto(id);
        }
    }
}
