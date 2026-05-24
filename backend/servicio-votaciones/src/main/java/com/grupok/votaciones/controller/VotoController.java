package com.grupok.votaciones.controller;

import com.grupok.votaciones.dto.EmitirVotoRequest;
import com.grupok.votaciones.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// CU1 — seq_cu1: API de Votaciones → :VotoController
@RestController
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    // CU1: POST /votos {usuarioId, respuestaId, valor}
    @PostMapping("/votos")
    public ResponseEntity<Map<String, Object>> emitirVoto(@Valid @RequestBody EmitirVotoRequest request) {
        int nuevoScore = votoService.emitirVoto(request.usuarioId(), request.respuestaId(), request.valor());
        return ResponseEntity.ok(Map.of("nuevoScore", nuevoScore));
    }

    // CU1: GET /votos/scores?ids=1,2,3 — scores calculados desde los votos almacenados
    @GetMapping("/votos/scores")
    public ResponseEntity<Map<Long, Integer>> obtenerScores(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(votoService.calcularScores(ids));
    }
}
