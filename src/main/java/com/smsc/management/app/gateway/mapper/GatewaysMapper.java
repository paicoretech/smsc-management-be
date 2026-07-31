package com.smsc.management.app.gateway.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smsc.management.app.gateway.dto.GatewaysDTO;
import com.smsc.management.app.gateway.dto.ParseGatewaysDTO;
import com.smsc.management.app.gateway.model.entity.Gateways;

@Mapper(componentModel = "spring")
public interface GatewaysMapper {
	Gateways toEntity(GatewaysDTO ParseGatewaysDTO);
	ParseGatewaysDTO toDTO(Gateways gatewayEntity);
	GatewaysDTO toGatewaysDTO(Gateways gatewayEntity);
	List<ParseGatewaysDTO> toDTO(List<Gateways> gateways);
	void updateEntityFromDTO(GatewaysDTO dto, @MappingTarget Gateways entity);
}
