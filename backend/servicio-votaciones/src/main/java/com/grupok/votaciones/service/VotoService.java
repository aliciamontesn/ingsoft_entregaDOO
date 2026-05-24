package com.grupok.votaciones.service;

import com.grupok.votaciones.fake.FakeMessageBroker;
import com.grupok.votaciones.model.Voto;
import com.grupok.votaciones.repository.VotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final FakeMessageBroker fakeMessageBroker;
    private final RestTemplate restTemplate;

    @Value("${servicios.publicaciones.url}")
    private String publicacionesUrl;

    public VotoService(VotoRepository votoRepository,
                       FakeMessageBroker fakeMessageBroker,
                       RestTemplate restTemplate) {
        this.votoRepository = votoRepository;
        this.fakeMessageBroker = fakeMessageBroker;
        this.restTemplate = restTemplate;
    }

    // CU1: calcularScores — suma de votos por respuesta (fuente de verdad del score)
    public Map<Long, Integer> calcularScores(List<Long> respuestaIds) {
        Map<Long, Integer> scores = respuestaIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0));
        votoRepository.findAllByRespuestaIdIn(respuestaIds)
                .forEach(v -> scores.merge(v.getRespuestaId(), v.getValor(), Integer::sum));
        return scores;
    }

    // CU1: emitirVoto(usuarioId, respuestaId, valor, autorRespuestaId)
    @Transactional
    public int emitirVoto(Long usuarioId, Long respuestaId, int valor, Long autorRespuestaId) {
        if (valor != 1 && valor != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El valor del voto debe ser +1 o -1");
        }

        // CU1 paso 3: verificarNoAutovoto — usar autorId enviado por el frontend (evita llamada inter-servicio)
        if (autorRespuestaId != null && usuarioId.equals(autorRespuestaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes votar tu propia respuesta");
        }

        // Verificación secundaria contra servicio-publicaciones (si está accesible)
        if (autorRespuestaId == null) {
            String urlRespuesta = publicacionesUrl + "/respuestas/" + respuestaId;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> datos = restTemplate.getForObject(urlRespuesta, Map.class);
                if (datos != null) {
                    Object autorIdObj = datos.get("autorId");
                    Long autorId = autorIdObj instanceof Number ? ((Number) autorIdObj).longValue() : null;
                    if (usuarioId.equals(autorId)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes votar tu propia respuesta");
                    }
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (HttpClientErrorException.NotFound e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada: " + respuestaId);
            } catch (Exception e) {
                // servicio-publicaciones no accesible; no se puede verificar autovoto sin autorRespuestaId
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar el autor de la respuesta");
            }
        }

        // CU1 extensiones 4a/4b: lógica de voto previo
        Optional<Voto> votoExistente = votoRepository.findByUsuarioIdAndRespuestaId(usuarioId, respuestaId);
        int delta;
        String evento;

        if (votoExistente.isPresent()) {
            Voto voto = votoExistente.get();
            if (voto.getValor() == valor) {
                // Extensión 4a: mismo voto → retirar
                votoRepository.delete(voto);
                delta = -valor;
                evento = "VotoRetirado";
            } else {
                // Extensión 4b: voto contrario → sustituir
                int oldValor = voto.getValor();
                voto.setValor(valor);
                votoRepository.save(voto);
                delta = valor - oldValor;
                evento = "VotoCambiado";
            }
        } else {
            Voto voto = new Voto();
            voto.setUsuarioId(usuarioId);
            voto.setRespuestaId(respuestaId);
            voto.setValor(valor);
            votoRepository.save(voto);
            delta = valor;
            evento = "VotoEmitido";
        }

        // Actualizar score en servicio-publicaciones (puede no estar accesible en Railway)
        int nuevoScore;
        try {
            String urlScore = publicacionesUrl + "/publicaciones/" + respuestaId + "/score";
            Integer scoreRemoto = restTemplate.patchForObject(urlScore, delta, Integer.class);
            nuevoScore = scoreRemoto != null ? scoreRemoto : 0;
        } catch (Exception e) {
            // servicio-publicaciones no accesible: calcular score localmente
            nuevoScore = votoRepository.findAllByRespuestaId(respuestaId)
                    .stream().mapToInt(Voto::getValor).sum();
        }

        fakeMessageBroker.publish(evento, respuestaId);

        return nuevoScore;
    }
}
