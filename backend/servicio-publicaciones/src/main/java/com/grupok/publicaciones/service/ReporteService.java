package com.grupok.publicaciones.service;

import com.grupok.publicaciones.dto.ReporteResultadoDto;
import com.grupok.publicaciones.fake.FakeMessageBroker;
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
    private final FakeMessageBroker fakeMessageBroker;

    public ReporteService(PublicacionRepository publicacionRepository,
                          ReporteRepository reporteRepository,
                          FakeMessageBroker fakeMessageBroker) {
        this.publicacionRepository = publicacionRepository;
        this.reporteRepository = reporteRepository;
        this.fakeMessageBroker = fakeMessageBroker;
    }

    // CU2: reportarPublicacion(usuarioId, publicacionId, motivo)
    @Transactional
    public ReporteResultadoDto reportarPublicacion(Long usuarioId, Long publicacionId, String motivo) {
        var publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada: " + publicacionId));

        // CU2 precondición: la publicación debe estar visible (no eliminada ni oculta)
        if (publicacion.getEstado() == EstadoPublicacion.ELIMINADA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada: " + publicacionId);
        }
        if (publicacion.getEstado() == EstadoPublicacion.OCULTA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada: " + publicacionId);
        }

        if (usuarioId.equals(publicacion.getAutorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes reportar tu propia publicación");
        }

        if (reporteRepository.existsByUsuarioIdAndPublicacionId(usuarioId, publicacionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya has reportado esta publicación");
        }

        Reporte reporte = new Reporte();
        reporte.setUsuarioId(usuarioId);
        reporte.setPublicacionId(publicacionId);
        reporte.setMotivo(motivo);
        reporteRepository.save(reporte);

        // CU2 extensión 6a: auto-ocultar si se supera el límite de reportes
        long numReportes = reporteRepository.countByPublicacionId(publicacionId);
        boolean oculta = false;
        EstadoPublicacion estadoActual = publicacion.getEstado();
        if (numReportes >= LIMITE_REPORTES
                && (estadoActual == null || estadoActual == EstadoPublicacion.VISIBLE)) {
            publicacion.setEstado(EstadoPublicacion.OCULTA);
            publicacionRepository.save(publicacion);
            oculta = true;
            fakeMessageBroker.publish("publicacion_ocultada", publicacionId);
        }

        int reportesRestantes = (int) Math.max(0, LIMITE_REPORTES - numReportes);
        return new ReporteResultadoDto(numReportes, reportesRestantes, oculta);
    }
}
