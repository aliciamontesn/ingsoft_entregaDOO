package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;

// CU4 — seq_cu4: RespuestaService → :PublicacionRepository (para Respuesta)
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {
}
