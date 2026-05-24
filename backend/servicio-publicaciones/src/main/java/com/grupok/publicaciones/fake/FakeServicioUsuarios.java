package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Simula la actualizacion de reputacion del usuario
@Component
public class FakeServicioUsuarios {

    public void sumarPuntosReputacion(Long autorRespuestaId) {
        System.out.printf("[FakeServicioUsuarios] sumarPuntosReputacion autorRespuestaId=%d%n", autorRespuestaId);
    }
}
