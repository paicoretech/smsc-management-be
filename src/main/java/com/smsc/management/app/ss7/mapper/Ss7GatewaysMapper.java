package com.smsc.management.app.ss7.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;

@Mapper(componentModel = "spring")
public interface Ss7GatewaysMapper {
	Ss7Gateways toEntity(Ss7GatewaysDTO ss7GatewayEntity);
	void updateEntityFromDTO(Ss7GatewaysDTO dto, @MappingTarget Ss7Gateways entity);
	Ss7GatewaysDTO toDTO(Ss7Gateways ss7GatewayDTO);
	List<Ss7GatewaysDTO> toDTOList(List<Ss7Gateways> ss7GatewayDTO);
}
