package com.ahorrito.app.profile;

import com.ahorrito.app.profile.PerfilDTO;
import com.ahorrito.app.profile.Perfil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PerfilMapper {
    PerfilDTO toDTO(Perfil perfil);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Perfil toEntity(PerfilDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updateEntityFromDTO(PerfilDTO dto, @MappingTarget Perfil perfil);
}
