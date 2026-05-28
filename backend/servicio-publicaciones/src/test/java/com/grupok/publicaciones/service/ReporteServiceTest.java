package com.grupok.publicaciones.service;

import com.grupok.publicaciones.dto.ReporteResultadoDto;
import com.grupok.publicaciones.fake.FakeMessageBroker;
import com.grupok.publicaciones.model.EstadoPublicacion;
import com.grupok.publicaciones.model.Pregunta; 
import com.grupok.publicaciones.model.Reporte;
import com.grupok.publicaciones.repository.PublicacionRepository;
import com.grupok.publicaciones.repository.ReporteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;
    @Mock
    private ReporteRepository reporteRepository;
    @Mock
    private FakeMessageBroker fakeMessageBroker;

    @InjectMocks
    private ReporteService reporteService;

    @Mock
    private Pregunta publicacionMock;

    @BeforeEach
    void setUp() {
        lenient().when(publicacionMock.getId()).thenReturn(100L);
        lenient().when(publicacionMock.getAutorId()).thenReturn(10L); 
        lenient().when(publicacionMock.getEstado()).thenReturn(EstadoPublicacion.VISIBLE);
    }

    @Test
    void reportarPublicacion_CuandoNoExiste_DebeLanzarNotFound() {
        when(publicacionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> reporteService.reportarPublicacion(5L, 999L, "Spam"));
    }

    @Test
    void reportarPublicacion_CuandoEstaEliminada_DebeLanzarNotFound() {
        when(publicacionMock.getEstado()).thenReturn(EstadoPublicacion.ELIMINADA);
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(publicacionMock));
        assertThrows(ResponseStatusException.class, () -> reporteService.reportarPublicacion(5L, 100L, "Spam"));
    }

    @Test
    void reportarPublicacion_CuandoEstaOculta_DebeLanzarNotFound() {
        when(publicacionMock.getEstado()).thenReturn(EstadoPublicacion.OCULTA);
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(publicacionMock));
        assertThrows(ResponseStatusException.class, () -> reporteService.reportarPublicacion(5L, 100L, "Spam"));
    }

    @Test
    void reportarPublicacion_CuandoEsAutor_DebeLanzarForbidden() {
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(publicacionMock));
        assertThrows(ResponseStatusException.class, () -> reporteService.reportarPublicacion(10L, 100L, "Auto-reporte"));
    }

    @Test
    void reportarPublicacion_CuandoYaExisteUnReporteDelMismoUsuario_DebeLanzarConflict() {
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(publicacionMock));
        when(reporteRepository.existsByUsuarioIdAndPublicacionId(5L, 100L)).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> reporteService.reportarPublicacion(5L, 100L, "Repetido"));
    }

    @Test
    void reportarPublicacion_FlujoExitoso_SinAlcanzarLimite() {
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(publicacionMock));
        when(reporteRepository.existsByUsuarioIdAndPublicacionId(5L, 100L)).thenReturn(false);
        when(reporteRepository.countByPublicacionId(100L)).thenReturn(1L);

        ReporteResultadoDto resultado = reporteService.reportarPublicacion(5L, 100L, "Contenido inadecuado");

        assertNotNull(resultado);
        verify(reporteRepository, times(1)).save(any(Reporte.class));
        verifyNoInteractions(fakeMessageBroker);
    }

    @Test
    void reportarPublicacion_FlujoExitoso_AlcanzaLimiteDeTresYSeOculta() {
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(publicacionMock));
        when(reporteRepository.existsByUsuarioIdAndPublicacionId(5L, 100L)).thenReturn(false);
        when(reporteRepository.countByPublicacionId(100L)).thenReturn(3L);

        ReporteResultadoDto resultado = reporteService.reportarPublicacion(5L, 100L, "Tercer Reporte");

        assertNotNull(resultado);
        verify(publicacionMock, times(1)).setEstado(EstadoPublicacion.OCULTA);
        verify(publicacionRepository, times(1)).save(publicacionMock);
        verify(fakeMessageBroker, times(1)).publish("publicacion_ocultada", 100L);
    }
}