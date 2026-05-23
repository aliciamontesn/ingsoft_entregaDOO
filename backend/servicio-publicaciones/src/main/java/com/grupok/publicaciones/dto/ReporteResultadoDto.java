package com.grupok.publicaciones.dto;

public record ReporteResultadoDto(
        long numReportes,
        int reportesRestantes,
        boolean publicacionOculta
) {}
