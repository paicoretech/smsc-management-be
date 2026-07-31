package com.smsc.management.app.ss7.mapper;

import com.smsc.management.app.ss7.dto.TcapDTO;
import com.smsc.management.app.ss7.model.entity.Tcap;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TcapMapper {

    Tcap toEntity(TcapDTO tcapDTO);
    TcapDTO toDTO(Tcap tcap);
    void updateEntityFromTCapDTO(TcapDTO tcapDTO, @MappingTarget Tcap entity);
}
