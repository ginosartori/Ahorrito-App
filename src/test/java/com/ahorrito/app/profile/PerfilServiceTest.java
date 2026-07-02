package com.ahorrito.app.profile;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.profile.PerfilDTO;
import com.ahorrito.app.profile.Perfil;
import com.ahorrito.app.profile.PerfilMapper;
import com.ahorrito.app.profile.PerfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ahorrito.app.auth.Usuario;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepository repository;

    @Mock
    private PerfilMapper mapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private PerfilService service;

    private Perfil perfil;
    private PerfilDTO dto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("Gino").build();

        perfil = Perfil.builder()
                .id(1L)
                .nombre("Gino")
                .rango("Miembro Premium")
                .tema("classic-dark")
                .usuario(usuario)
                .build();

        dto = PerfilDTO.builder()
                .id(1L)
                .nombre("Gino")
                .rango("Miembro Premium")
                .tema("classic-dark")
                .build();
    }

    @Test
    void getPerfil_WhenNoneExists_ShouldCreateDefault() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(repository.findByUsuario(usuario)).thenReturn(Optional.empty());
        when(repository.save(any(Perfil.class))).thenReturn(perfil);
        when(mapper.toDTO(perfil)).thenReturn(dto);

        PerfilDTO result = service.getPerfil();

        assertNotNull(result);
        assertEquals("Gino", result.getNombre());
        assertEquals("Miembro Premium", result.getRango());
        verify(repository).save(any(Perfil.class));
    }

    @Test
    void getPerfil_WhenExists_ShouldReturnIt() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(perfil));
        when(mapper.toDTO(perfil)).thenReturn(dto);

        PerfilDTO result = service.getPerfil();

        assertNotNull(result);
        assertEquals("Gino", result.getNombre());
        verify(repository, never()).save(any(Perfil.class));
    }

    @Test
    void updatePerfil_ShouldUpdateAndReturn() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(perfil));
        when(repository.save(perfil)).thenReturn(perfil);
        when(mapper.toDTO(perfil)).thenReturn(dto);

        PerfilDTO request = PerfilDTO.builder()
                .nombre("Andres")
                .rango("Ahorrador Experto")
                .tema("crema-beige")
                .build();

        PerfilDTO result = service.updatePerfil(request);

        assertNotNull(result);
        verify(mapper).updateEntityFromDTO(request, perfil);
        verify(repository).save(perfil);
    }

    @Test
    void updatePerfil_ShouldNotModifyUsuarioCredentials() {
        // Arrange: usuario con credenciales conocidas
        String originalUsername = "Gino";
        String originalPassword = "hashedPassword123";
        usuario.setPassword(originalPassword);

        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(perfil));
        when(repository.save(perfil)).thenReturn(perfil);
        when(mapper.toDTO(perfil)).thenReturn(dto);

        // Act: actualizar nombre y apodo del perfil
        PerfilDTO request = PerfilDTO.builder()
                .nombre("Nombre Nuevo")
                .rango("Apodo Nuevo")
                .build();

        service.updatePerfil(request);

        // Assert: las credenciales del usuario NO fueron modificadas
        assertEquals(originalUsername, usuario.getUsername(),
                "El username del usuario NO debe cambiar al actualizar el perfil");
        assertEquals(originalPassword, usuario.getPassword(),
                "El password del usuario NO debe cambiar al actualizar el perfil");

        // Verificar que el mapper ignora el campo 'usuario' (según @Mapping(target = "usuario", ignore = true))
        verify(mapper).updateEntityFromDTO(request, perfil);
        // El perfil mantiene la referencia al mismo usuario sin modificaciones
        assertSame(usuario, perfil.getUsuario(),
                "La referencia al Usuario dentro del Perfil no debe cambiar");
    }
}
