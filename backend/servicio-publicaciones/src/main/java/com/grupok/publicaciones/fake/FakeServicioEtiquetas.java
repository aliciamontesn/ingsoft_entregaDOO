package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

import java.util.List;

// Simula la validacion de etiquetas; en produccion consultaria al servicio real
@Component
public class FakeServicioEtiquetas {

    public List<Long> validarEtiquetas(List<Long> etiquetaIds) {
        return etiquetaIds;
    }
}
