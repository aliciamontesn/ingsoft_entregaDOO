package com.grupok.publicaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicarRespuestaRequest(
        @NotNull Long usuarioId,
        @NotNull Long preguntaId,
        @NotBlank String contenido
) {}
