package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.service.PublicacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// CU1 — seq_cu1: recibe PATCH /publicaciones/{id}/score desde servicio-votaciones
@RestController
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    // CU1: PATCH /publicaciones/{respuestaId}/score {delta}
    @PatchMapping("/publicaciones/{id}/score")
    public ResponseEntity<Integer> actualizarScore(@PathVariable Long id,
                                                   @RequestBody int delta) {
        int nuevoScore = publicacionService.actualizarScore(id, delta);
        return ResponseEntity.ok(nuevoScore);
    }
}
