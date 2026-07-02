package com.ahorrito.app.checklist;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GastoChecklistMapper {

    @Mapping(target = "carteraId", source = "cartera.id")
    @Mapping(target = "carteraNombre", source = "cartera.nombre")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "transaccionId", source = "transaccion.id")
    GastoChecklistResponseDTO toResponseDTO(GastoChecklist entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cartera", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "transaccion", ignore = true)
    @Mapping(target = "completado", ignore = true)
    GastoChecklist toEntity(GastoChecklistRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cartera", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "transaccion", ignore = true)
    @Mapping(target = "completado", ignore = true)
    void updateEntityFromDTO(GastoChecklistRequestDTO requestDTO, @MappingTarget GastoChecklist entity);
}
