package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.dto.ReportarPublicacionRequest;
import com.grupok.publicaciones.model.Reporte;
import com.grupok.publicaciones.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                                       @RequestBody ReportarPublicacionRequest request) {
        Reporte reporte = reporteService.reportarPublicacion(request.usuarioId(), id, request.motivo());
        return ResponseEntity.ok(reporte);
    }
}
