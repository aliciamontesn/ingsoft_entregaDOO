package com.grupok.publicaciones.service;

import com.grupok.publicaciones.dto.PreguntaDetalleDto;
import com.grupok.publicaciones.dto.PreguntaResumenDto;
import com.grupok.publicaciones.fake.FakeMessageBroker;
import com.grupok.publicaciones.fake.FakeServicioEtiquetas;
import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Respuesta;
import com.grupok.publicaciones.repository.PreguntaRepository;
import com.grupok.publicaciones.repository.RespuestaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    private final FakeServicioEtiquetas fakeServicioEtiquetas;
    private final FakeMessageBroker fakeMessageBroker;

    public PreguntaService(PreguntaRepository preguntaRepository,
                           RespuestaRepository respuestaRepository,
                           FakeServicioEtiquetas fakeServicioEtiquetas,
                           FakeMessageBroker fakeMessageBroker) {
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.fakeServicioEtiquetas = fakeServicioEtiquetas;
        this.fakeMessageBroker = fakeMessageBroker;
    }

    @Transactional
    public Pregunta publicarPregunta(Long usuarioId, String titulo, String contenido, List<Long> etiquetaIds) {
        List<Long> ids = etiquetaIds != null ? etiquetaIds : new ArrayList<>();
        fakeServicioEtiquetas.validarEtiquetas(ids);

        Pregunta pregunta = new Pregunta();
        pregunta.setAutorId(usuarioId);
        pregunta.setTitulo(titulo);
        pregunta.setContenido(contenido);
        pregunta.setEtiquetaIds(ids);

        Pregunta guardada = preguntaRepository.save(pregunta);
        fakeMessageBroker.publish("pregunta_publicada", guardada.getId());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<PreguntaResumenDto> listarPreguntas() {
        return preguntaRepository.findAll().stream()
                .filter(p -> {
                    EstadoPublicacion e = p.getEstado();
                    return e == null || e == EstadoPublicacion.VISIBLE;
                })
                .map(this::toResumen)
                .collect(Collectors.toList());
    }

    private PreguntaResumenDto toResumen(Pregunta p) {
        long numRespuestas = respuestaRepository
                .countByPreguntaIdAndEstadoNot(p.getId(), EstadoPublicacion.ELIMINADA);
        return new PreguntaResumenDto(
                p.getId(), p.getAutorId(), p.getScore(), p.getTitulo(), p.getContenido(),
                p.getEtiquetaIds(), p.getAcceptedAnswerId(), p.getFechaCreacion(), p.getEstado(),
                numRespuestas);
    }

    @Transactional(readOnly = true)
    public PreguntaDetalleDto obtenerDetalle(Long preguntaId) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada: " + preguntaId));

        if (pregunta.getEstado() == EstadoPublicacion.ELIMINADA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada: " + preguntaId);
        }

        List<Respuesta> respuestas = respuestaRepository.findByPreguntaId(preguntaId).stream()
                .filter(r -> r.getEstado() != EstadoPublicacion.ELIMINADA)
                .collect(Collectors.toList());

        return new PreguntaDetalleDto(pregunta, respuestas);
    }

    @Transactional
    public void eliminarPregunta(Long preguntaId, Long usuarioId) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada: " + preguntaId));

        if (pregunta.getEstado() == EstadoPublicacion.ELIMINADA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada: " + preguntaId);
        }
        if (!pregunta.getAutorId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede eliminar su pregunta");
        }

        pregunta.setEstado(EstadoPublicacion.ELIMINADA);
        preguntaRepository.save(pregunta);
        fakeMessageBroker.publish("pregunta_eliminada", preguntaId);
    }
}
