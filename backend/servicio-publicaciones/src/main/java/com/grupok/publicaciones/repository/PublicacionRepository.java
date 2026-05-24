package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    @Query("SELECT r.pregunta FROM Respuesta r WHERE r.id = :respuestaId")
    Optional<Pregunta> findPreguntaByRespuestaId(@Param("respuestaId") Long respuestaId);
}
