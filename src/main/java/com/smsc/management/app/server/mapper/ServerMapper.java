package com.smsc.management.app.server.mapper;

import com.paicbd.smsc.dto.SmppServerConfig;
import com.smsc.management.app.server.dto.SmppServerDTO;
import com.smsc.management.app.server.model.entity.SmppServer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServerMapper {
    SmppServer toEntity(SmppServerDTO smppServerConfig);
    SmppServerDTO toDTO(SmppServer smppServer);
    List<SmppServerDTO> toDTO(List<SmppServer> smppServers);
    void updateEntityFromDTO(SmppServerDTO smppServerConfig, @MappingTarget SmppServer smppServer);

    SmppServerConfig toRedisDTOFromEntity(SmppServer smppServer);
}
