package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.dto.AceptarRespuestaRequest;
import com.grupok.publicaciones.dto.PublicarRespuestaRequest;
import com.grupok.publicaciones.model.Respuesta;
import com.grupok.publicaciones.service.RespuestaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @GetMapping("/respuestas/{id}")
    public ResponseEntity<Respuesta> obtenerRespuesta(@PathVariable Long id) {
        return ResponseEntity.ok(respuestaService.obtenerRespuesta(id));
    }

    @PostMapping("/respuestas")
    public ResponseEntity<Respuesta> publicarRespuesta(@Valid @RequestBody PublicarRespuestaRequest request) {
        Respuesta creada = respuestaService.publicarRespuesta(
                request.usuarioId(), request.preguntaId(), request.contenido());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/respuestas/{id}/aceptar")
    public ResponseEntity<Void> aceptarRespuesta(@PathVariable Long id,
                                                  @Valid @RequestBody AceptarRespuestaRequest request) {
        respuestaService.aceptarRespuesta(request.usuarioId(), id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/respuestas/{id}/desaceptar")
    public ResponseEntity<Void> desaceptarRespuesta(@PathVariable Long id,
                                                     @Valid @RequestBody AceptarRespuestaRequest request) {
        respuestaService.desaceptarRespuesta(request.usuarioId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/respuestas/{id}")
    public ResponseEntity<Void> eliminarRespuesta(@PathVariable Long id,
                                                   @RequestParam Long usuarioId) {
        respuestaService.eliminarRespuesta(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
