package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Simula el servicio de busqueda (en produccion seria Elasticsearch)
@Component
public class FakeServicioBusqueda {

    public void indexarEnElasticsearch(Long preguntaId) {
        System.out.printf("[FakeServicioBusqueda] indexarEnElasticsearch preguntaId=%d%n", preguntaId);
    }

    public void eliminarDelIndice(Long publicacionId) {
        System.out.printf("[FakeServicioBusqueda] eliminarDelIndice publicacionId=%d%n", publicacionId);
    }
}
