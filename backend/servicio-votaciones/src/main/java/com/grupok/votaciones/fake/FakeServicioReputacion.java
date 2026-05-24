package com.grupok.votaciones.fake;

import org.springframework.stereotype.Component;

// Simula la actualizacion de reputacion cuando se recibe un voto
@Component
public class FakeServicioReputacion {

    public void actualizarReputacion(Long autorId, int delta) {
        System.out.printf("[FakeServicioReputacion] actualizarReputacion autorId=%d delta=%d%n", autorId, delta);
    }
}
