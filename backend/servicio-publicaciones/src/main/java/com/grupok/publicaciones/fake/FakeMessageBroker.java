package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Message Broker — CU3 y CU4: publish(evento)
// Simula la publicación asíncrona de eventos sin infraestructura real de mensajería
@Component
public class FakeMessageBroker {

    public void publish(String evento, Object payload) {
        System.out.printf("[FakeMessageBroker] evento='%s' payload=%s%n", evento, payload);
    }
}
