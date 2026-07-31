package com.smsc.management.app.broadcast.mapper;

import com.smsc.management.app.broadcast.dto.BroadcastDTO;
import com.smsc.management.app.broadcast.model.entity.Broadcast;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = ColumnMappingMapper.class)
public interface BroadcastMapper {
    @Mapping(target = "columnMapping", source = "columnMapping")
    Broadcast toEntity(BroadcastDTO broadcastDTO);

    List<Broadcast> toEntity(List<BroadcastDTO> broadcastDTO);

    BroadcastDTO toDto(Broadcast broadcast);

    @Mapping(target = "columnMapping", source = "columnMapping")
    void updateEntityFromDTO(BroadcastDTO dto, @MappingTarget Broadcast entity);
}
