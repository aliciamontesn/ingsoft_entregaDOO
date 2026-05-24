package com.grupok.votaciones.fake;

import org.springframework.stereotype.Component;

// Simula la notificacion al autor cuando recibe un voto
@Component
public class FakeServicioNotificaciones {

    public void notificarAutorVoto(Long autorId) {
        System.out.printf("[FakeServicioNotificaciones] notificarAutorVoto autorId=%d%n", autorId);
    }
}
