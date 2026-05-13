package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.dto.AceptarRespuestaRequest;
import com.grupok.publicaciones.service.RespuestaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// CU4 — seq_cu4: API de Publicaciones → :RespuestaController
@RestController
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    // CU4: PATCH /respuestas/{id}/aceptar {usuarioId}
    @PatchMapping("/respuestas/{id}/aceptar")
    public ResponseEntity<Void> aceptarRespuesta(@PathVariable Long id,
                                                 @RequestBody AceptarRespuestaRequest request) {
        respuestaService.aceptarRespuesta(request.usuarioId(), id);
        return ResponseEntity.ok().build();
    }
}
