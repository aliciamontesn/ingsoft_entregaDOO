package com.grupok.publicaciones.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Respuesta extends Publicacion {

    private String contenido;
    private boolean esAceptada = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id")
    private Pregunta pregunta;

    // FK directa para serialización sin lazy load
    @Column(name = "pregunta_id", insertable = false, updatable = false)
    private Long preguntaId;

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public boolean isEsAceptada() { return esAceptada; }
    public void setEsAceptada(boolean esAceptada) { this.esAceptada = esAceptada; }

    @JsonIgnore
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }

    public Long getPreguntaId() { return preguntaId; }
}
