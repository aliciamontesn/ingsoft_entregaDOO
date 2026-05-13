package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Servicio de Búsqueda — CU3: consume pregunta_publicada → indexarEnElasticsearch
@Component
public class FakeServicioBusqueda {

    public void indexarEnElasticsearch(Long preguntaId) {
        System.out.printf("[FakeServicioBusqueda] indexarEnElasticsearch preguntaId=%d%n", preguntaId);
    }
}
