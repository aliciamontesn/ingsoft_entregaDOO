package com.grupok.publicaciones.service;

import com.grupok.publicaciones.model.Publicacion;
import com.grupok.publicaciones.repository.PublicacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// CU1 (llamada desde servicio-votaciones): PATCH /publicaciones/{id}/score
@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;

    public PublicacionService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public int actualizarScore(Long publicacionId, int delta) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada: " + publicacionId));
        publicacion.setScore(publicacion.getScore() + delta);
        publicacionRepository.save(publicacion);
        return publicacion.getScore();
    }
}
