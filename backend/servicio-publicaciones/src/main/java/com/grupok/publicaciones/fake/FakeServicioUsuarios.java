package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Servicio de Usuarios — CU4: consume respuesta_aceptada → sumarPuntosReputacion
@Component
public class FakeServicioUsuarios {

    public void sumarPuntosReputacion(Long autorRespuestaId) {
        System.out.printf("[FakeServicioUsuarios] sumarPuntosReputacion autorRespuestaId=%d%n", autorRespuestaId);
    }
}
