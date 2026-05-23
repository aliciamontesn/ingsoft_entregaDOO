package com.grupok.publicaciones.fake;

import org.springframework.stereotype.Component;

// Fake del Servicio de Búsqueda — CU3: consume pregunta_publicada → indexarEnElasticsearch; CU2: consume publicacion_ocultada → eliminarDelIndice
@Component
public class FakeServicioBusqueda {

    public void indexarEnElasticsearch(Long preguntaId) {
        System.out.printf("[FakeServicioBusqueda] indexarEnElasticsearch preguntaId=%d%n", preguntaId);
    }

    public void eliminarDelIndice(Long publicacionId) {
        System.out.printf("[FakeServicioBusqueda] eliminarDelIndice publicacionId=%d%n", publicacionId);
    }
}
