package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.dto.PreguntaDetalleDto;
import com.grupok.publicaciones.dto.PreguntaResumenDto;
import com.grupok.publicaciones.dto.PublicarPreguntaRequest;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.service.PreguntaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @GetMapping("/preguntas")
    public ResponseEntity<List<PreguntaResumenDto>> listarPreguntas() {
        return ResponseEntity.ok(preguntaService.listarPreguntas());
    }

    @GetMapping("/preguntas/{id}")
    public ResponseEntity<PreguntaDetalleDto> obtenerPregunta(@PathVariable Long id) {
        return ResponseEntity.ok(preguntaService.obtenerDetalle(id));
    }

    @PostMapping("/preguntas")
    public ResponseEntity<Pregunta> publicarPregunta(@Valid @RequestBody PublicarPreguntaRequest request) {
        Pregunta creada = preguntaService.publicarPregunta(
                request.usuarioId(), request.titulo(), request.contenido(), request.etiquetaIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/preguntas/{id}")
    public ResponseEntity<Void> eliminarPregunta(@PathVariable Long id,
                                                  @RequestParam Long usuarioId) {
        preguntaService.eliminarPregunta(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
