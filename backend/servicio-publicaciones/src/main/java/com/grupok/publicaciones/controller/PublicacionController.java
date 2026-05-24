package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.service.PublicacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Este endpoint lo llama servicio-votaciones cuando registra un voto
@RestController
public class PublicacionController {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @PatchMapping("/publicaciones/{id}/score")
    public ResponseEntity<Integer> actualizarScore(@PathVariable Long id,
                                                   @RequestBody int delta) {
        int nuevoScore = publicacionService.actualizarScore(id, delta);
        return ResponseEntity.ok(nuevoScore);
    }
}
