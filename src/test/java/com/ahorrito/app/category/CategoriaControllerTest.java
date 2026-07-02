package com.ahorrito.app.category;

import tools.jackson.databind.ObjectMapper;
import com.ahorrito.app.category.CategoriaRequestDTO;
import com.ahorrito.app.category.CategoriaResponseDTO;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.category.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@WithMockUser
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService categoriaService;

    private CategoriaResponseDTO responseDTO;
    private CategoriaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new CategoriaResponseDTO(1L, "Comida", "Gastos de alimentación", "purple", null);
        requestDTO = new CategoriaRequestDTO("Comida", "Gastos de alimentación", "purple", null);
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(categoriaService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Comida"));

        verify(categoriaService).findAll();
    }

    @Test
    void getById_WhenExists_ShouldReturnCategoria() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Comida"));

        verify(categoriaService).findById(1L);
    }

    @Test
    void getById_WhenDoesNotExist_ShouldReturn404() throws Exception {
        when(categoriaService.findById(1L)).thenThrow(new ResourceNotFoundException("Categoría no encontrada con id: 1"));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Categoría no encontrada con id: 1"));

        verify(categoriaService).findById(1L);
    }

    @Test
    void create_WithValidDTO_ShouldReturnCreated() throws Exception {
        when(categoriaService.create(any(CategoriaRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Comida"));

        verify(categoriaService).create(any(CategoriaRequestDTO.class));
    }

    @Test
    void create_WithInvalidDTO_ShouldReturnBadRequest() throws Exception {
        CategoriaRequestDTO invalidDTO = new CategoriaRequestDTO("", "Descripcion", null, null); // Nombre vacío es inválido

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.nombre").exists());

        verify(categoriaService, never()).create(any());
    }

    @Test
    void update_WithValidDTO_ShouldReturnUpdated() throws Exception {
        when(categoriaService.update(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Comida"));

        verify(categoriaService).update(eq(1L), any(CategoriaRequestDTO.class));
    }

    @Test
    void delete_WhenExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(categoriaService).delete(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).delete(1L);
    }
}
