package com.grupok.publicaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PublicarPreguntaRequest(
        @NotNull Long usuarioId,
        @NotBlank String titulo,
        @NotBlank String contenido,
        List<Long> etiquetaIds
) {}
