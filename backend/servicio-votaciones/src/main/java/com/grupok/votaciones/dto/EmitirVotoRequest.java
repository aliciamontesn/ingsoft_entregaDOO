package com.grupok.votaciones.dto;

public record EmitirVotoRequest(Long usuarioId, Long respuestaId, int valor) {}
