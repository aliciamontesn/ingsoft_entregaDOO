package com.grupok.votaciones.dto;

import jakarta.validation.constraints.NotNull;

public record EmitirVotoRequest(
        @NotNull Long usuarioId,
        @NotNull Long respuestaId,
        int valor,
        Long autorRespuestaId  // enviado por el frontend; evita llamada inter-servicio para check autovoto
) {}
