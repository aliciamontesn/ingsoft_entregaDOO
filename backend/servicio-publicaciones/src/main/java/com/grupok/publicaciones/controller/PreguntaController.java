package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.service.PreguntaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// CU3 — seq_cu3: API de Publicaciones → :PreguntaController
@RestController
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    // CU3: POST /preguntas {usuarioId, titulo, contenido, etiquetaIds}
    @PostMapping("/preguntas")
    public ResponseEntity<Pregunta> publicarPregunta(@RequestBody Map<String, Object> body) {
        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
        String titulo = (String) body.get("titulo");
        String contenido = (String) body.get("contenido");
        @SuppressWarnings("unchecked")
        List<Long> etiquetaIds = (List<Long>) body.get("etiquetaIds");

        Pregunta creada = preguntaService.publicarPregunta(usuarioId, titulo, contenido, etiquetaIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
