package com.ahorrito.app.category;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.category.CategoriaRequestDTO;
import com.ahorrito.app.category.CategoriaResponseDTO;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.category.CategoriaMapper;
import com.ahorrito.app.category.CategoriaRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findAll() {
        Usuario usuario = securityService.getLoggedUser();
        return categoriaRepository.findAllByUsuario(usuario).stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@securityService.isCategoriaOwner(#id)")
    public CategoriaResponseDTO findById(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return categoriaMapper.toResponseDTO(categoria);
    }

    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        if (categoriaRepository.existsByNombreIgnoreCaseAndUsuario(requestDTO.nombre(), usuario)) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + requestDTO.nombre());
        }
        Categoria categoria = categoriaMapper.toEntity(requestDTO);
        categoria.setUsuario(usuario);
        Categoria saved = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(saved);
    }

    @Transactional
    @PreAuthorize("@securityService.isCategoriaOwner(#id)")
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        if (!categoria.getNombre().equalsIgnoreCase(requestDTO.nombre()) 
                && categoriaRepository.existsByNombreIgnoreCaseAndUsuario(requestDTO.nombre(), usuario)) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + requestDTO.nombre());
        }

        categoriaMapper.updateEntityFromDTO(requestDTO, categoria);
        return categoriaMapper.toResponseDTO(categoria);
    }

    @Transactional
    @PreAuthorize("@securityService.isCategoriaOwner(#id)")
    public void delete(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        categoriaRepository.delete(categoria);
    }
}
