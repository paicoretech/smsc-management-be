package com.smsc.management.app.ss7.mapper;

import com.smsc.management.app.ss7.dto.MapDTO;
import com.smsc.management.app.ss7.model.entity.Map;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MapMapper {

    Map toEntity(MapDTO mapDTO);
    MapDTO toDTO(Map map);
    void updateEntityFromMapDTO(MapDTO mapDTO, @MappingTarget Map entity);
}
