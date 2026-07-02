package com.ahorrito.app.wallet;

import com.ahorrito.app.wallet.CarteraRequestDTO;
import com.ahorrito.app.wallet.CarteraResponseDTO;
import com.ahorrito.app.wallet.Cartera;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarteraMapper {

    CarteraResponseDTO toResponseDTO(Cartera cartera);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Cartera toEntity(CarteraRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updateEntityFromDTO(CarteraRequestDTO requestDTO, @MappingTarget Cartera cartera);
}
