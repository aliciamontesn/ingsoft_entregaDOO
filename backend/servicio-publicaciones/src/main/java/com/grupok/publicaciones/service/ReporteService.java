package com.grupok.publicaciones.service;

import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Reporte;
import com.grupok.publicaciones.repository.PublicacionRepository;
import com.grupok.publicaciones.repository.ReporteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// CU2 — seq_cu2: :ReporteController → :ReporteService
@Service
public class ReporteService {

    private static final int LIMITE_REPORTES = 3;

    private final PublicacionRepository publicacionRepository;
    private final ReporteRepository reporteRepository;

    public ReporteService(PublicacionRepository publicacionRepository,
                          ReporteRepository reporteRepository) {
        this.publicacionRepository = publicacionRepository;
        this.reporteRepository = reporteRepository;
    }

    // CU2: reportarPublicacion(usuarioId, publicacionId, motivo)
    @Transactional
    public Reporte reportarPublicacion(Long usuarioId, Long publicacionId, String motivo) {
        var publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada: " + publicacionId));

        Reporte reporte = new Reporte();
        reporte.setUsuarioId(usuarioId);
        reporte.setPublicacionId(publicacionId);
        reporte.setMotivo(motivo);
        reporteRepository.save(reporte);

        // CU2 extensión 6a: auto-ocultar si se supera el límite de reportes
        long numReportes = reporteRepository.countByPublicacionId(publicacionId);
        EstadoPublicacion estadoActual = publicacion.getEstado();
        if (numReportes >= LIMITE_REPORTES
                && (estadoActual == null || estadoActual == EstadoPublicacion.VISIBLE)) {
            publicacion.setEstado(EstadoPublicacion.OCULTA);
            publicacionRepository.save(publicacion);
        }

        return reporte;
    }
}
