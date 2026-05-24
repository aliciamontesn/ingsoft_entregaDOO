package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.dto.ReportarPublicacionRequest;
import com.grupok.publicaciones.dto.ReporteResultadoDto;
import com.grupok.publicaciones.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping("/publicaciones/{id}/reportes")
    public ResponseEntity<ReporteResultadoDto> reportarPublicacion(@PathVariable Long id,
                                                                   @Valid @RequestBody ReportarPublicacionRequest request) {
        ReporteResultadoDto resultado = reporteService.reportarPublicacion(request.usuarioId(), id, request.motivo());
        return ResponseEntity.ok(resultado);
    }
}
