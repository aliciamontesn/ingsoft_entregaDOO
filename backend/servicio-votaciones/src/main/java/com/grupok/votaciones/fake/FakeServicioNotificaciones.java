package com.grupok.votaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Servicio de Notificaciones — CU1: consume VotoEmitido → notificarAutorVoto
@Component
public class FakeServicioNotificaciones {

    public void notificarAutorVoto(Long autorId) {
        System.out.printf("[FakeServicioNotificaciones] notificarAutorVoto autorId=%d%n", autorId);
    }
}
