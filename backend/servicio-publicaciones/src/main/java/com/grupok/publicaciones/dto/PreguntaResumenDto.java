package com.grupok.publicaciones.dto;

import com.grupok.publicaciones.model.EstadoPublicacion;

import java.time.LocalDateTime;
import java.util.List;

public record PreguntaResumenDto(
        Long id,
        Long autorId,
        int score,
        String titulo,
        String contenido,
        List<Long> etiquetaIds,
        Long acceptedAnswerId,
        LocalDateTime fechaCreacion,
        EstadoPublicacion estado,
        long numRespuestas
) {}
