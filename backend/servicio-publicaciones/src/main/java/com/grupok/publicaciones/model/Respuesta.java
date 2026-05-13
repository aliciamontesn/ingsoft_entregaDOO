package com.grupok.publicaciones.model;

import jakarta.persistence.*;

@Entity
public class Respuesta extends Publicacion {

    private String contenido;
    private boolean esAceptada = false;

    @ManyToOne
    @JoinColumn(name = "pregunta_id")
    private Pregunta pregunta;

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public boolean isEsAceptada() { return esAceptada; }
    public void setEsAceptada(boolean esAceptada) { this.esAceptada = esAceptada; }
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
}
