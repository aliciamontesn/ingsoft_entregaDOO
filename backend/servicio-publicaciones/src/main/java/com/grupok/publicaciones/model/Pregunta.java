package com.grupok.publicaciones.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Pregunta extends Publicacion {

    private String titulo;
    private String contenido;
    private Long acceptedAnswerId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> etiquetaIds;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public Long getAcceptedAnswerId() { return acceptedAnswerId; }
    public void setAcceptedAnswerId(Long acceptedAnswerId) { this.acceptedAnswerId = acceptedAnswerId; }
    public List<Long> getEtiquetaIds() { return etiquetaIds; }
    public void setEtiquetaIds(List<Long> etiquetaIds) { this.etiquetaIds = etiquetaIds; }
}
