package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

import java.util.List;

// Fake del Servicio de Etiquetas — CU3: llamada síncrona desde PreguntaService
// Simula GET /etiquetas?ids={etiquetaIds} devolviendo siempre etiquetas válidas
@Component
public class FakeServicioEtiquetas {

    public List<Long> validarEtiquetas(List<Long> etiquetaIds) {
        return etiquetaIds;
    }
}
