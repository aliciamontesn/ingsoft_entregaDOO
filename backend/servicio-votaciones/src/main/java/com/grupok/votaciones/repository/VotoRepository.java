package com.grupok.votaciones.repository;

import com.grupok.votaciones.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// CU1 — seq_cu1: VotoService → :VotoRepository
public interface VotoRepository extends JpaRepository<Voto, Long> {

    // CU1: buscarVotoPrevio(usuarioId, respuestaId)
    Optional<Voto> findByUsuarioIdAndRespuestaId(Long usuarioId, Long respuestaId);
}
