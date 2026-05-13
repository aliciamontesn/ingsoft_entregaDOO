package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.model.Reporte;
import com.grupok.publicaciones.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// CU2 — seq_cu2: API de Publicaciones → :ReporteController
@RestController
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // CU2: POST /publicaciones/{id}/reportes {usuarioId, motivo}
    @PostMapping("/publicaciones/{id}/reportes")
    public ResponseEntity<Reporte> reportarPublicacion(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> body) {
        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
        String motivo = (String) body.get("motivo");

        Reporte reporte = reporteService.reportarPublicacion(usuarioId, id, motivo);
        return ResponseEntity.ok(reporte);
    }
}
