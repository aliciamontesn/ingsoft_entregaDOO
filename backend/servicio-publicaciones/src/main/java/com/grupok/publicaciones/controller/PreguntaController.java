package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.dto.PublicarPreguntaRequest;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.service.PreguntaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// CU3 — seq_cu3: API de Publicaciones → :PreguntaController
@RestController
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    // CU3: POST /preguntas {usuarioId, titulo, contenido, etiquetaIds}
    @PostMapping("/preguntas")
    public ResponseEntity<Pregunta> publicarPregunta(@RequestBody PublicarPreguntaRequest request) {
        Pregunta creada = preguntaService.publicarPregunta(
                request.usuarioId(), request.titulo(), request.contenido(), request.etiquetaIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
