package com.grupok.publicaciones.service;

import com.grupok.publicaciones.model.Reporte;
import com.grupok.publicaciones.repository.PublicacionRepository;
import com.grupok.publicaciones.repository.ReporteRepository;
import org.springframework.stereotype.Service;

// CU2 — seq_cu2: :ReporteController → :ReporteService
@Service
public class ReporteService {

    private final PublicacionRepository publicacionRepository;
    private final ReporteRepository reporteRepository;

    public ReporteService(PublicacionRepository publicacionRepository,
                          ReporteRepository reporteRepository) {
        this.publicacionRepository = publicacionRepository;
        this.reporteRepository = reporteRepository;
    }

    // CU2: reportarPublicacion(usuarioId, publicacionId, motivo)
    public Reporte reportarPublicacion(Long usuarioId, Long publicacionId, String motivo) {
        publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada: " + publicacionId));

        Reporte reporte = new Reporte();
        reporte.setUsuarioId(usuarioId);
        reporte.setPublicacionId(publicacionId);
        reporte.setMotivo(motivo);
        reporteRepository.save(reporte);

        long numReportes = reporteRepository.countByPublicacionId(publicacionId);
        // Si se supera el límite de reportes, aquí se gestionaría la moderación
        return reporte;
    }
}
