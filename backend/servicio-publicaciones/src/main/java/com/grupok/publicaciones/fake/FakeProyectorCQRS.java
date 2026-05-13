package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Proyector CQRS — CU3 y CU4: consume eventos → actualizarModeloLectura
@Component
public class FakeProyectorCQRS {

    public void actualizarModeloLectura(String evento, Long id) {
        System.out.printf("[FakeProyectorCQRS] actualizarModeloLectura evento='%s' id=%d%n", evento, id);
    }
}
