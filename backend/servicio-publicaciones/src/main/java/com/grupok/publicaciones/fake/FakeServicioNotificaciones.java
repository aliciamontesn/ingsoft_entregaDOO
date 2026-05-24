package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Simula el servicio de notificaciones
@Component
public class FakeServicioNotificaciones {

    public void notificarAutorRespuesta(Long autorRespuestaId) {
        System.out.printf("[FakeServicioNotificaciones] notificarAutorRespuesta autorRespuestaId=%d%n", autorRespuestaId);
    }

    public void notificarAdministradores(Long publicacionId) {
        System.out.printf("[FakeServicioNotificaciones] notificarAdministradores publicacionId=%d%n", publicacionId);
    }
}
