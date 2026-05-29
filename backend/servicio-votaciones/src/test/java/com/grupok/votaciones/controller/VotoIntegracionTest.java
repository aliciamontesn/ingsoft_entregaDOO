package com.grupok.votaciones.controller;

import com.grupok.votaciones.service.VotoService;
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

@WebMvcTest(VotoController.class)
class VotoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VotoService votoService;

    @Test
    void emitirVoto_CuandoDatosSonValidos_DebeRetornar200YContenerNuevoScore() throws Exception {
        when(votoService.emitirVoto(anyLong(), anyLong(), anyInt(), any()))
                .thenReturn(5);

        mockMvc.perform(post("/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "usuarioId": 10,
                                    "respuestaId": 45,
                                    "valor": 1,
                                    "autorRespuestaId": 99
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuevoScore").value(5));
    }

    @Test
    void emitirVoto_CuandoFaltanCamposObligatorios_DebeRetornar400BadRequest() throws Exception {
        mockMvc.perform(post("/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
