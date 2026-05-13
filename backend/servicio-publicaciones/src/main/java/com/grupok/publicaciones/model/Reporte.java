package com.grupok.publicaciones.model;

import jakarta.persistence.*;

@Entity
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long publicacionId;
    private Long usuarioId;
    private String motivo;

    public Long getId() { return id; }
    public Long getPublicacionId() { return publicacionId; }
    public void setPublicacionId(Long publicacionId) { this.publicacionId = publicacionId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
