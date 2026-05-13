package com.grupok.votaciones.service;

import com.grupok.votaciones.fake.FakeMessageBroker;
import com.grupok.votaciones.model.Voto;
import com.grupok.votaciones.repository.VotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// CU1 — seq_cu1: :VotoController → :VotoService
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
    public int emitirVoto(Long usuarioId, Long respuestaId, int valor) {
        // CU1: buscarVotoPrevio(usuarioId, respuestaId)
        votoRepository.findByUsuarioIdAndRespuestaId(usuarioId, respuestaId)
                .ifPresent(v -> { throw new IllegalStateException("El usuario ya votó esta publicación"); });

        // CU1: guardar(Voto)
        Voto voto = new Voto();
        voto.setUsuarioId(usuarioId);
        voto.setRespuestaId(respuestaId);
        voto.setValor(valor);
        votoRepository.save(voto);

        // CU1: PATCH /publicaciones/{respuestaId}/score {delta} — llamada al microservicio real
        String url = publicacionesUrl + "/publicaciones/" + respuestaId + "/score";
        Integer nuevoScore = restTemplate.patchForObject(url, valor, Integer.class);

        fakeMessageBroker.publish("VotoEmitido", respuestaId);

        return nuevoScore != null ? nuevoScore : 0;
    }
}
