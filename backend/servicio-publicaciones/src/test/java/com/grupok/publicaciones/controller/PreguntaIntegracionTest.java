package com.grupok.publicaciones.controller;

import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.service.PreguntaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PreguntaController.class)
class PreguntaIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PreguntaService preguntaService;

    @Test
    void publicarPregunta_CuandoDatosSonValidos_DebeRetornar201Created() throws Exception {
        Pregunta preguntaCreada = new Pregunta();
        preguntaCreada.setTitulo("Error en herencia JPA con estrategia JOINED");
        when(preguntaService.publicarPregunta(anyLong(), anyString(), anyString(), any()))
                .thenReturn(preguntaCreada);

        mockMvc.perform(post("/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "usuarioId": 10,
                                    "titulo": "Error en herencia JPA con estrategia JOINED",
                                    "contenido": "No se están persistiendo correctamente los atributos.",
                                    "etiquetaIds": [1, 2]
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void publicarPregunta_CuandoTituloOContenidoSonVacios_DebeRetornar400BadRequest() throws Exception {
        mockMvc.perform(post("/preguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "usuarioId": 10,
                                    "titulo": "",
                                    "contenido": "",
                                    "etiquetaIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
