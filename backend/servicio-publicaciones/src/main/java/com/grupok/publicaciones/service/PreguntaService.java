package com.grupok.publicaciones.service;

import com.grupok.publicaciones.fake.FakeMessageBroker;
import com.grupok.publicaciones.fake.FakeServicioEtiquetas;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.repository.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// CU3 — seq_cu3: :PreguntaController → :PreguntaService
@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final FakeServicioEtiquetas fakeServicioEtiquetas;
    private final FakeMessageBroker fakeMessageBroker;

    public PreguntaService(PreguntaRepository preguntaRepository,
                           FakeServicioEtiquetas fakeServicioEtiquetas,
                           FakeMessageBroker fakeMessageBroker) {
        this.preguntaRepository = preguntaRepository;
        this.fakeServicioEtiquetas = fakeServicioEtiquetas;
        this.fakeMessageBroker = fakeMessageBroker;
    }

    // CU3: publicarPregunta(usuarioId, titulo, contenido, etiquetaIds)
    public Pregunta publicarPregunta(Long usuarioId, String titulo, String contenido, List<Long> etiquetaIds) {
        fakeServicioEtiquetas.validarEtiquetas(etiquetaIds);

        Pregunta pregunta = new Pregunta();
        pregunta.setAutorId(usuarioId);
        pregunta.setTitulo(titulo);
        pregunta.setContenido(contenido);
        pregunta.setEtiquetaIds(etiquetaIds);

        Pregunta guardada = preguntaRepository.save(pregunta);

        fakeMessageBroker.publish("pregunta_publicada", guardada.getId());

        return guardada;
    }
}
