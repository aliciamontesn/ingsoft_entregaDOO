package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    List<Respuesta> findByPreguntaId(Long preguntaId);

    long countByPreguntaIdAndEstadoNot(Long preguntaId, EstadoPublicacion estado);
}
