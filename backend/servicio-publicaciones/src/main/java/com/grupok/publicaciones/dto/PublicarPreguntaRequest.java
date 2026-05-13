package com.grupok.publicaciones.dto;

import java.util.List;

public record PublicarPreguntaRequest(Long usuarioId, String titulo, String contenido, List<Long> etiquetaIds) {}
