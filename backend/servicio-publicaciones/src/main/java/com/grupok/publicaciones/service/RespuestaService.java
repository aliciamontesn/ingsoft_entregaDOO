package com.grupok.publicaciones.service;

import com.grupok.publicaciones.fake.FakeMessageBroker;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Respuesta;
import com.grupok.publicaciones.repository.PublicacionRepository;
import org.springframework.stereotype.Service;

// CU4 — seq_cu4: :RespuestaController → :RespuestaService
@Service
public class RespuestaService {

    private final PublicacionRepository publicacionRepository;
    private final FakeMessageBroker fakeMessageBroker;

    public RespuestaService(PublicacionRepository publicacionRepository,
                            FakeMessageBroker fakeMessageBroker) {
        this.publicacionRepository = publicacionRepository;
        this.fakeMessageBroker = fakeMessageBroker;
    }

    // CU4: aceptarRespuesta(usuarioId, respuestaId)
    public void aceptarRespuesta(Long usuarioId, Long respuestaId) {
        // CU4: buscarPreguntaPorRespuesta(respuestaId)
        Pregunta pregunta = publicacionRepository.findPreguntaByRespuestaId(respuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada: " + respuestaId));

        if (!pregunta.getAutorId().equals(usuarioId)) {
            throw new IllegalStateException("Solo el autor de la pregunta puede aceptar una respuesta");
        }

        // CU4: marcarRespuestaAceptada(respuestaId)
        Respuesta respuesta = (Respuesta) publicacionRepository.findById(respuestaId)
                .orElseThrow();
        respuesta.setEsAceptada(true);
        pregunta.setAcceptedAnswerId(respuestaId);
        publicacionRepository.save(respuesta);
        publicacionRepository.save(pregunta);

        fakeMessageBroker.publish("respuesta_aceptada", respuestaId);
    }
}
