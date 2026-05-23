package com.grupok.publicaciones.dto;

import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Respuesta;

import java.util.List;

public record PreguntaDetalleDto(Pregunta pregunta, List<Respuesta> respuestas) {}
