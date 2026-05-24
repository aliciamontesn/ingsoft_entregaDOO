package com.grupok.votaciones.controller;

import com.grupok.votaciones.dto.EmitirVotoRequest;
import com.grupok.votaciones.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping("/votos")
    public ResponseEntity<Map<String, Object>> emitirVoto(@Valid @RequestBody EmitirVotoRequest request) {
        int nuevoScore = votoService.emitirVoto(request.usuarioId(), request.respuestaId(), request.valor(), request.autorRespuestaId());
        return ResponseEntity.ok(Map.of("nuevoScore", nuevoScore));
    }

    // scores calculados sumando los votos, no desde publicaciones
    @GetMapping("/votos/scores")
    public ResponseEntity<Map<Long, Integer>> obtenerScores(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(votoService.calcularScores(ids));
    }
}
