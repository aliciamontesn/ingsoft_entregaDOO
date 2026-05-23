package com.grupok.publicaciones.model;

public enum EstadoPublicacion {
    VISIBLE,
    OCULTA,      // oculta automáticamente por límite de reportes (CU2 extensión 6a)
    ELIMINADA    // borrada por su autor (soft delete)
}
