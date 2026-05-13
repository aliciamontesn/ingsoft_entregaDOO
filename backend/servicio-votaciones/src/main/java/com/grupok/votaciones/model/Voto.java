package com.grupok.votaciones.model;

import jakarta.persistence.*;

@Entity
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;
    private Long respuestaId;
    private int valor; // +1 o -1

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getRespuestaId() { return respuestaId; }
    public void setRespuestaId(Long respuestaId) { this.respuestaId = respuestaId; }
    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }
}
