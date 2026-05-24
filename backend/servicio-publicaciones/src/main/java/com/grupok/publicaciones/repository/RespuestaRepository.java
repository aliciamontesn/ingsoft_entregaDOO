package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    List<Respuesta> findByPreguntaId(Long preguntaId);

    long countByPreguntaIdAndEstadoNot(Long preguntaId, EstadoPublicacion estado);

    // excluye las ocultas y eliminadas del conteo que aparece en la lista de preguntas
    @Query("SELECT COUNT(r) FROM Respuesta r WHERE r.preguntaId = :id AND (r.estado IS NULL OR r.estado = 'VISIBLE')")
    long countVisibleByPreguntaId(@Param("id") Long preguntaId);
}
