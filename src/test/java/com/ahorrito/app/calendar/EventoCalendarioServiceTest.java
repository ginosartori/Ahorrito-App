package com.ahorrito.app.calendar;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.calendar.EventoCalendarioRequestDTO;
import com.ahorrito.app.calendar.EventoCalendarioResponseDTO;
import com.ahorrito.app.calendar.EventoCalendario;
import com.ahorrito.app.calendar.EventoCalendarioMapper;
import com.ahorrito.app.calendar.EventoCalendarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ahorrito.app.auth.Usuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoCalendarioServiceTest {

    @Mock
    private EventoCalendarioRepository repository;

    @Mock
    private EventoCalendarioMapper mapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private EventoCalendarioService service;

    private EventoCalendario evento;
    private EventoCalendarioRequestDTO requestDTO;
    private EventoCalendarioResponseDTO responseDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("testuser").build();

        evento = EventoCalendario.builder()
                .id(1L)
                .titulo("Vencimiento Tarjeta")
                .fecha(LocalDate.now())
                .tipo("PAGO")
                .usuario(usuario)
                .build();

        requestDTO = new EventoCalendarioRequestDTO("Vencimiento Tarjeta", null, LocalDate.now(), "PAGO");
        responseDTO = new EventoCalendarioResponseDTO(1L, "Vencimiento Tarjeta", null, LocalDate.now(), "PAGO");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(repository.findAllByUsuario(usuario)).thenReturn(List.of(evento));
        when(mapper.toResponseDTO(evento)).thenReturn(responseDTO);

        List<EventoCalendarioResponseDTO> result = service.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Vencimiento Tarjeta", result.get(0).titulo());
    }

    @Test
    void delete_WhenExists_ShouldDelete() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(repository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(evento));

        assertDoesNotThrow(() -> service.delete(1L));

        verify(repository).delete(evento);
    }
}
