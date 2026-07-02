package com.ahorrito.app.category;

import com.ahorrito.app.category.CategoriaRequestDTO;
import com.ahorrito.app.category.CategoriaResponseDTO;
import com.ahorrito.app.category.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaResponseDTO toResponseDTO(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Categoria toEntity(CategoriaRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updateEntityFromDTO(CategoriaRequestDTO requestDTO, @MappingTarget Categoria categoria);
}
