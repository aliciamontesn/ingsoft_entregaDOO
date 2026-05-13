package com.grupok.publicaciones.service;

import com.grupok.publicaciones.fake.FakeMessageBroker;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Respuesta;
import com.grupok.publicaciones.repository.PublicacionRepository;
import com.grupok.publicaciones.repository.RespuestaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// CU4 — seq_cu4: :RespuestaController → :RespuestaService
@Service
public class RespuestaService {

    private final PublicacionRepository publicacionRepository;
    private final RespuestaRepository respuestaRepository;
    private final FakeMessageBroker fakeMessageBroker;

    public RespuestaService(PublicacionRepository publicacionRepository,
                            RespuestaRepository respuestaRepository,
                            FakeMessageBroker fakeMessageBroker) {
        this.publicacionRepository = publicacionRepository;
        this.respuestaRepository = respuestaRepository;
        this.fakeMessageBroker = fakeMessageBroker;
    }

    // CU4: aceptarRespuesta(usuarioId, respuestaId)
    public void aceptarRespuesta(Long usuarioId, Long respuestaId) {
        // CU4: buscarPreguntaPorRespuesta(respuestaId)
        Pregunta pregunta = publicacionRepository.findPreguntaByRespuestaId(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));

        if (!pregunta.getAutorId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor de la pregunta puede aceptar una respuesta");
        }

        // CU4 extensión 5a: ya existe una respuesta aceptada
        if (pregunta.getAcceptedAnswerId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pregunta ya tiene una respuesta aceptada");
        }

        // CU4: marcarRespuestaAceptada(respuestaId)
        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));
        respuesta.setEsAceptada(true);
        pregunta.setAcceptedAnswerId(respuestaId);

        respuestaRepository.save(respuesta);
        publicacionRepository.save(pregunta);

        fakeMessageBroker.publish("respuesta_aceptada", respuestaId);
    }
}
