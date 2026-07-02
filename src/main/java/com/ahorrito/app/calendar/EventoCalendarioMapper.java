package com.ahorrito.app.calendar;

import com.ahorrito.app.calendar.EventoCalendarioRequestDTO;
import com.ahorrito.app.calendar.EventoCalendarioResponseDTO;
import com.ahorrito.app.calendar.EventoCalendario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventoCalendarioMapper {

    EventoCalendarioResponseDTO toResponseDTO(EventoCalendario evento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    EventoCalendario toEntity(EventoCalendarioRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updateEntityFromDTO(EventoCalendarioRequestDTO requestDTO, @MappingTarget EventoCalendario evento);
}
