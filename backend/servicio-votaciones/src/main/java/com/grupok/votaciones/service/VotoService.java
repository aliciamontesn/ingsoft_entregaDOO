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

import java.util.Map;
import java.util.Optional;

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

    // CU1: emitirVoto(usuarioId, respuestaId, valor)
    @Transactional
    public int emitirVoto(Long usuarioId, Long respuestaId, int valor) {
        if (valor != 1 && valor != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El valor del voto debe ser +1 o -1");
        }

        // CU1 paso 3: verificarNoAutovoto — consulta autorId de la respuesta
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
            // No se puede alcanzar servicio-publicaciones; se omite la verificación de autovoto
        }

        // CU1 extensiones 4a/4b: lógica de voto previo
        Optional<Voto> votoExistente = votoRepository.findByUsuarioIdAndRespuestaId(usuarioId, respuestaId);
        int delta;

        if (votoExistente.isPresent()) {
            Voto voto = votoExistente.get();
            if (voto.getValor() == valor) {
                // Extensión 4a: mismo voto → retirar
                votoRepository.delete(voto);
                delta = -valor;
            } else {
                // Extensión 4b: voto contrario → sustituir
                int oldValor = voto.getValor();
                voto.setValor(valor);
                votoRepository.save(voto);
                delta = valor - oldValor;
            }
        } else {
            Voto voto = new Voto();
            voto.setUsuarioId(usuarioId);
            voto.setRespuestaId(respuestaId);
            voto.setValor(valor);
            votoRepository.save(voto);
            delta = valor;
        }

        // Actualizar score en servicio-publicaciones
        String urlScore = publicacionesUrl + "/publicaciones/" + respuestaId + "/score";
        Integer nuevoScore = restTemplate.patchForObject(urlScore, delta, Integer.class);

        fakeMessageBroker.publish("VotoEmitido", respuestaId);

        return nuevoScore != null ? nuevoScore : 0;
    }
}
