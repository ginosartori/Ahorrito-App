package com.ahorrito.app.category;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.category.CategoriaRequestDTO;
import com.ahorrito.app.category.CategoriaResponseDTO;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.category.CategoriaMapper;
import com.ahorrito.app.category.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ahorrito.app.auth.Usuario;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaRequestDTO requestDTO;
    private CategoriaResponseDTO responseDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("testuser").build();

        categoria = Categoria.builder()
                .id(1L)
                .nombre("Comida")
                .descripcion("Gastos de alimentación")
                .usuario(usuario)
                .build();

        requestDTO = new CategoriaRequestDTO("Comida", "Gastos de alimentación", "purple", null);
        responseDTO = new CategoriaResponseDTO(1L, "Comida", "Gastos de alimentación", "purple", null);
    }

    @Test
    void findAll_ShouldReturnList() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.findAllByUsuario(usuario)).thenReturn(List.of(categoria));
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        List<CategoriaResponseDTO> result = categoriaService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Comida", result.get(0).nombre());
        verify(categoriaRepository).findAllByUsuario(usuario);
    }

    @Test
    void findById_WhenIdExists_ShouldReturnCategoria() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(categoriaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    void findById_WhenIdDoesNotExist_ShouldThrowException() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaService.findById(1L));
        verify(categoriaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    void create_WhenNameIsUnique_ShouldSaveCategoria() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.existsByNombreIgnoreCaseAndUsuario(requestDTO.nombre(), usuario)).thenReturn(false);
        when(categoriaMapper.toEntity(requestDTO)).thenReturn(categoria);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.create(requestDTO);

        assertNotNull(result);
        assertEquals("Comida", result.nombre());
        verify(categoriaRepository).existsByNombreIgnoreCaseAndUsuario("Comida", usuario);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void create_WhenNameAlreadyExists_ShouldThrowIllegalArgumentException() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.existsByNombreIgnoreCaseAndUsuario(requestDTO.nombre(), usuario)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoriaService.create(requestDTO));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void update_WhenIdExistsAndNameIsUnique_ShouldUpdateCategoria() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toResponseDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO result = categoriaService.update(1L, requestDTO);

        assertNotNull(result);
        verify(categoriaMapper).updateEntityFromDTO(requestDTO, categoria);
    }

    @Test
    void delete_WhenIdExists_ShouldDeleteCategoria() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));

        assertDoesNotThrow(() -> categoriaService.delete(1L));
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void delete_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaService.delete(1L));
        verify(categoriaRepository, never()).delete(any());
    }
}
