package com.grupok.publicaciones.service;

import com.grupok.publicaciones.fake.FakeMessageBroker;
import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Respuesta;
import com.grupok.publicaciones.repository.PreguntaRepository;
import com.grupok.publicaciones.repository.PublicacionRepository;
import com.grupok.publicaciones.repository.RespuestaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RespuestaService {

    private final PublicacionRepository publicacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    private final FakeMessageBroker fakeMessageBroker;

    public RespuestaService(PublicacionRepository publicacionRepository,
                            PreguntaRepository preguntaRepository,
                            RespuestaRepository respuestaRepository,
                            FakeMessageBroker fakeMessageBroker) {
        this.publicacionRepository = publicacionRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.fakeMessageBroker = fakeMessageBroker;
    }

    @Transactional
    public void aceptarRespuesta(Long usuarioId, Long respuestaId) {
        Pregunta pregunta = publicacionRepository.findPreguntaByRespuestaId(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));

        if (!pregunta.getAutorId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor de la pregunta puede aceptar una respuesta");
        }
        if (pregunta.getAcceptedAnswerId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pregunta ya tiene una respuesta aceptada. Puedes quitarla primero.");
        }

        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));
        respuesta.setEsAceptada(true);
        pregunta.setAcceptedAnswerId(respuestaId);

        respuestaRepository.save(respuesta);
        publicacionRepository.save(pregunta);
        fakeMessageBroker.publish("respuesta_aceptada", respuestaId);
    }

    @Transactional
    public void desaceptarRespuesta(Long usuarioId, Long respuestaId) {
        Pregunta pregunta = publicacionRepository.findPreguntaByRespuestaId(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));

        if (!pregunta.getAutorId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor de la pregunta puede quitar la respuesta aceptada");
        }
        if (!respuestaId.equals(pregunta.getAcceptedAnswerId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta respuesta no es la respuesta aceptada");
        }

        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));
        respuesta.setEsAceptada(false);
        pregunta.setAcceptedAnswerId(null);

        respuestaRepository.save(respuesta);
        publicacionRepository.save(pregunta);
        fakeMessageBroker.publish("respuesta_desaceptada", respuestaId);
    }

    @Transactional
    public Respuesta publicarRespuesta(Long usuarioId, Long preguntaId, String contenido) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada: " + preguntaId));

        if (pregunta.getEstado() == EstadoPublicacion.ELIMINADA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada: " + preguntaId);
        }
        if (pregunta.getEstado() == EstadoPublicacion.OCULTA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede responder una publicación bajo revisión");
        }
        if (pregunta.getAutorId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes responder tu propia pregunta");
        }

        Respuesta respuesta = new Respuesta();
        respuesta.setAutorId(usuarioId);
        respuesta.setContenido(contenido);
        respuesta.setPregunta(pregunta);
        Respuesta guardada = respuestaRepository.save(respuesta);
        fakeMessageBroker.publish("respuesta_publicada", guardada.getId());
        return guardada;
    }

    @Transactional(readOnly = true)
    public Respuesta obtenerRespuesta(Long id) {
        return respuestaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + id));
    }

    @Transactional
    public void eliminarRespuesta(Long respuestaId, Long usuarioId) {
        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId));

        if (respuesta.getEstado() == EstadoPublicacion.ELIMINADA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId);
        }

        Pregunta pregunta = preguntaRepository.findById(respuesta.getPreguntaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada"));

        boolean esAutorRespuesta = respuesta.getAutorId().equals(usuarioId);
        boolean esAutorPregunta  = pregunta.getAutorId().equals(usuarioId);
        if (!esAutorRespuesta && !esAutorPregunta) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta respuesta");
        }

        respuesta.setEstado(EstadoPublicacion.ELIMINADA);
        respuestaRepository.save(respuesta);
        fakeMessageBroker.publish("respuesta_eliminada", respuestaId);
    }
}
