package com.smsc.management.app.dnd.mapper;

import com.smsc.management.app.broadcast.mapper.ColumnMappingMapper;
import com.smsc.management.app.dnd.dto.DndEntryListsResponseDTO;
import com.smsc.management.app.dnd.dto.DndRequestDTO;
import com.smsc.management.app.dnd.model.entity.DndEntryList;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = ColumnMappingMapper.class)
public interface DndMapper {
    DndEntryList toEntity(DndRequestDTO dto);
    DndEntryListsResponseDTO toDTO(DndEntryList entity);
    List<DndEntryListsResponseDTO> toDtoList(List<DndEntryList> entities);
}