package com.grupok.publicaciones.dto;

import jakarta.validation.constraints.NotNull;

public record AceptarRespuestaRequest(@NotNull Long usuarioId) {}
