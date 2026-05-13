package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

// CU3 — seq_cu3: PreguntaService → :PreguntaRepository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
}
