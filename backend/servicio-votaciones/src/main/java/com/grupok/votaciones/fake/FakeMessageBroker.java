package com.grupok.votaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Message Broker — CU1: publish(VotoEmitido)
// Simula la publicación asíncrona de eventos sin infraestructura real de mensajería
@Component
public class FakeMessageBroker {

    public void publish(String evento, Object payload) {
        System.out.printf("[FakeMessageBroker] evento='%s' payload=%s%n", evento, payload);
    }
}
