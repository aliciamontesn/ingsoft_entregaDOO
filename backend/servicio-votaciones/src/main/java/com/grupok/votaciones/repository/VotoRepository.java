package com.grupok.votaciones.repository;

import com.grupok.votaciones.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    Optional<Voto> findByUsuarioIdAndRespuestaId(Long usuarioId, Long respuestaId);

    // para calcular el score localmente si el otro servicio no responde
    java.util.List<Voto> findAllByRespuestaId(Long respuestaId);

    java.util.List<Voto> findAllByRespuestaIdIn(java.util.Collection<Long> respuestaIds);
}
