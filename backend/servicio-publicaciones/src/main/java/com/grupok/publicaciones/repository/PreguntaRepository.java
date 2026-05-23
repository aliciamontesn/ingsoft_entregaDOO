package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByEstado(EstadoPublicacion estado);
}
