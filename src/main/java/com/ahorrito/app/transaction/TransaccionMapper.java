package com.ahorrito.app.transaction;
import com.ahorrito.app.category.CategoriaMapper;
import com.ahorrito.app.wallet.CarteraMapper;

import com.ahorrito.app.transaction.TransaccionRequestDTO;
import com.ahorrito.app.transaction.TransaccionResponseDTO;
import com.ahorrito.app.transaction.Transaccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, CarteraMapper.class})
public interface TransaccionMapper {

    TransaccionResponseDTO toResponseDTO(Transaccion transaccion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cartera", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Transaccion toEntity(TransaccionRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cartera", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    void updateEntityFromDTO(TransaccionRequestDTO requestDTO, @MappingTarget Transaccion transaccion);
}
