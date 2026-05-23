package com.grupok.publicaciones.repository;

import com.grupok.publicaciones.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

// CU2 — seq_cu2: ReporteService → :ReporteRepository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    // CU2: contarPorPublicacion(publicacionId)
    long countByPublicacionId(Long publicacionId);

    boolean existsByUsuarioIdAndPublicacionId(Long usuarioId, Long publicacionId);
}
