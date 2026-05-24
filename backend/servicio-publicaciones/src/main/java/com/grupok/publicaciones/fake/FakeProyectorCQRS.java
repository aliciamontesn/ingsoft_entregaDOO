package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Simula el proyector CQRS que actualiza el modelo de lectura
@Component
public class FakeProyectorCQRS {

    public void actualizarModeloLectura(String evento, Long id) {
        System.out.printf("[FakeProyectorCQRS] actualizarModeloLectura evento='%s' id=%d%n", evento, id);
    }
}
