package com.ahorrito.app.profile;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerfilController.class)
@WithMockUser
class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PerfilService perfilService;

    private PerfilDTO perfilDTO;

    @BeforeEach
    void setUp() {
        perfilDTO = PerfilDTO.builder()
                .id(1L)
                .nombre("Gino")
                .rango("Miembro Premium")
                .tema("classic-dark")
                .build();
    }

    @Test
    void getPerfil_ShouldReturnPerfil() throws Exception {
        when(perfilService.getPerfil()).thenReturn(perfilDTO);

        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Gino"))
                .andExpect(jsonPath("$.rango").value("Miembro Premium"))
                .andExpect(jsonPath("$.tema").value("classic-dark"));

        verify(perfilService).getPerfil();
    }

    @Test
    void updatePerfil_ShouldReturnUpdatedPerfil() throws Exception {
        PerfilDTO updatedDTO = PerfilDTO.builder()
                .id(1L)
                .nombre("Gino Editado")
                .rango("Ahorrador Experto")
                .tema("crema-beige")
                .build();

        when(perfilService.updatePerfil(any(PerfilDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(perfilDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Gino Editado"))
                .andExpect(jsonPath("$.rango").value("Ahorrador Experto"))
                .andExpect(jsonPath("$.tema").value("crema-beige"));

        verify(perfilService).updatePerfil(any(PerfilDTO.class));
    }

    @Test
    void updatePerfil_WithInvalidName_ShouldReturnBadRequest() throws Exception {
        PerfilDTO invalidDTO = PerfilDTO.builder()
                .nombre("") // Nombre vacío
                .rango("Ahorrador Experto")
                .tema("crema-beige")
                .build();

        mockMvc.perform(put("/api/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(perfilService, never()).updatePerfil(any(PerfilDTO.class));
    }
}
