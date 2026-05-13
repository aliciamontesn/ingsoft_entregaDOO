package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Servicio de Notificaciones — CU4: consume respuesta_aceptada → notificarAutorRespuesta
@Component
public class FakeServicioNotificaciones {

    public void notificarAutorRespuesta(Long autorRespuestaId) {
        System.out.printf("[FakeServicioNotificaciones] notificarAutorRespuesta autorRespuestaId=%d%n", autorRespuestaId);
    }
}
