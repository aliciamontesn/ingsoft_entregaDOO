package com.grupok.votaciones.controller;

import com.grupok.votaciones.service.VotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> emitirVoto(@RequestBody Map<String, Object> body) {
        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
        Long respuestaId = Long.valueOf(body.get("respuestaId").toString());
        int valor = Integer.parseInt(body.get("valor").toString());

        int nuevoScore = votoService.emitirVoto(usuarioId, respuestaId, valor);
        return ResponseEntity.ok(Map.of("nuevoScore", nuevoScore));
    }
}
