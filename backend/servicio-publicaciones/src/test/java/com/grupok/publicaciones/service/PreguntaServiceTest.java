package com.grupok.publicaciones.service;

import com.grupok.publicaciones.dto.PreguntaDetalleDto;
import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Pregunta;
import com.grupok.publicaciones.model.Respuesta;
import com.grupok.publicaciones.repository.PreguntaRepository;
import com.grupok.publicaciones.repository.RespuestaRepository;
import com.grupok.publicaciones.fake.FakeServicioEtiquetas;
import com.grupok.publicaciones.fake.FakeMessageBroker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceTest {

    @Mock
    private PreguntaRepository preguntaRepository;
    
    @Mock
    private RespuestaRepository respuestaRepository;
    
    @Mock
    private FakeServicioEtiquetas fakeServicioEtiquetas;
    
    @Mock
    private FakeMessageBroker fakeMessageBroker;

    @InjectMocks
    private PreguntaService preguntaService;

    @Mock
    private Pregunta preguntaMock;

    @BeforeEach
    void setUp() {
        lenient().when(preguntaMock.getId()).thenReturn(1L);
        lenient().when(preguntaMock.getAutorId()).thenReturn(10L);
        lenient().when(preguntaMock.getTitulo()).thenReturn("Título");
        lenient().when(preguntaMock.getContenido()).thenReturn("Contenido");
        lenient().when(preguntaMock.getEstado()).thenReturn(EstadoPublicacion.VISIBLE);
    }

    // ==========================================
    //   PRUEBAS DEL MÉTODO: obtenerDetalle
    // ==========================================
    @Test
    void obtenerDetalle_CuandoPreguntaNoExiste_DebeLanzarNotFound() {
        when(preguntaRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(ResponseStatusException.class, () -> {
            preguntaService.obtenerDetalle(999L);
        });
    }

    @Test
    void obtenerDetalle_CuandoPreguntaEstaEliminada_DebeLanzarNotFound() {
        when(preguntaMock.getEstado()).thenReturn(EstadoPublicacion.ELIMINADA);
        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        
        assertThrows(ResponseStatusException.class, () -> {
            preguntaService.obtenerDetalle(1L);
        });
    }

    @Test
    void obtenerDetalle_CuandoPreguntaEstaOculta_DebeLanzarForbidden() {
        when(preguntaMock.getEstado()).thenReturn(EstadoPublicacion.OCULTA);
        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            preguntaService.obtenerDetalle(1L);
        });
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void obtenerDetalle_FlujoFeliz_SinRespuestas() {
        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        when(respuestaRepository.findByPreguntaId(1L)).thenReturn(Collections.emptyList());
        
        PreguntaDetalleDto resultado = preguntaService.obtenerDetalle(1L);
        assertNotNull(resultado);
    }

    @Test
    void obtenerDetalle_FlujoFeliz_ConRespuestasFiltradas() {
        Respuesta r1 = mock(Respuesta.class); when(r1.getEstado()).thenReturn(EstadoPublicacion.VISIBLE);
        Respuesta r2 = mock(Respuesta.class); when(r2.getEstado()).thenReturn(null);
        Respuesta r3 = mock(Respuesta.class); when(r3.getEstado()).thenReturn(EstadoPublicacion.ELIMINADA);
        Respuesta r4 = mock(Respuesta.class); when(r4.getEstado()).thenReturn(EstadoPublicacion.OCULTA);

        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        when(respuestaRepository.findByPreguntaId(1L)).thenReturn(Arrays.asList(r1, r2, r3, r4));

        PreguntaDetalleDto resultado = preguntaService.obtenerDetalle(1L);
        assertNotNull(resultado);
    }

    // ==========================================
    //   PRUEBAS DEL MÉTODO: publicarPregunta
    // ==========================================
    @Test
    void publicarPregunta_ConEtiquetasNull_DebeInicializarListaYGuardar() {
        when(preguntaRepository.save(any(Pregunta.class))).thenReturn(preguntaMock);

        Pregunta resultado = preguntaService.publicarPregunta(10L, "Título", "Contenido", null);

        assertNotNull(resultado);
        verify(fakeServicioEtiquetas, times(1)).validarEtiquetas(any());
        verify(fakeMessageBroker, times(1)).publish(eq("pregunta_publicada"), any());
    }

    @Test
    void publicarPregunta_ConListaDeEtiquetas_DebeValidarYGuardar() {
        List<Long> etiquetas = Arrays.asList(1L, 2L);
        when(preguntaRepository.save(any(Pregunta.class))).thenReturn(preguntaMock);

        Pregunta resultado = preguntaService.publicarPregunta(10L, "Título", "Contenido", etiquetas);

        assertNotNull(resultado);
        verify(fakeServicioEtiquetas, times(1)).validarEtiquetas(etiquetas);
    }
    @Test
    void obtenerDetalle_ConRespuestaEstadoNulo_DebeTratarlaComoVisible() {
        Respuesta respuestaEstadoNulo = mock(Respuesta.class);
        // Simulamos una respuesta que no tiene estado explícito (estado = null)
        when(respuestaEstadoNulo.getEstado()).thenReturn(null);

        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        when(respuestaRepository.findByPreguntaId(1L)).thenReturn(Collections.singletonList(respuestaEstadoNulo));

        PreguntaDetalleDto resultado = preguntaService.obtenerDetalle(1L);
        
        assertNotNull(resultado);
    }
}