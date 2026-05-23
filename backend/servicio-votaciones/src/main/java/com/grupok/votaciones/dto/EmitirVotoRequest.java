package com.grupok.votaciones.dto;

import jakarta.validation.constraints.NotNull;

public record EmitirVotoRequest(
        @NotNull Long usuarioId,
        @NotNull Long respuestaId,
        int valor
) {}
