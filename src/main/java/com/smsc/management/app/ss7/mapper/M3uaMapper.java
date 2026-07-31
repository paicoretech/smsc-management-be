package com.smsc.management.app.ss7.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaDTO;
import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.dto.M3uaSocketsDTO;
import com.smsc.management.app.ss7.model.entity.M3ua;
import com.smsc.management.app.ss7.model.entity.M3uaApplicationServer;
import com.smsc.management.app.ss7.model.entity.M3uaAssociations;
import com.smsc.management.app.ss7.model.entity.M3uaRoutes;
import com.smsc.management.app.ss7.model.entity.M3uaSockets;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface M3uaMapper {
	M3ua toEntity(M3uaDTO m3ua);
	M3uaDTO toDTO(M3ua m3uaEntity);
	List<M3uaDTO> toDTOList(List<M3ua> m3uaList);
	void updateEntityFromM3uaDTO(M3uaDTO m3ua, @MappingTarget M3ua entity);
	
	// servers
	M3uaSockets toEntityServer(M3uaSocketsDTO m3uaServer);
	M3uaSocketsDTO toDTOServer(M3uaSockets m3uaServerEntity);
	List<M3uaSocketsDTO> toDTOServerList(List<M3uaSockets> m3uaServerEntity);
	void updateEntityFromServersDTO(M3uaSocketsDTO m3uaServer, @MappingTarget M3uaSockets entity);

	// associations
	M3uaAssociations toEntityAssociation(M3uaAssociationsDTO m3uaAssociationDTO);
	M3uaAssociationsDTO toDTOAssociation(M3uaAssociations m3uaAssociationEntity);
	void updateEntityFromAssociationsDTO(M3uaAssociationsDTO m3uaAssociationDTO, @MappingTarget M3uaAssociations entity);
	
	// application servers
	M3uaApplicationServer toEntityAppServer(M3uaApplicationServerDTO m3uaASDTO);
	M3uaApplicationServerDTO toDTOAppServer(M3uaApplicationServer m3uaAS);
	void updateEntityFromAppDTO(M3uaApplicationServerDTO m3uaASDTO, @MappingTarget M3uaApplicationServer entity);
	
	// routes
	M3uaRoutes toEntityRoutes(M3uaRoutesDTO m3uaRouteDTO);
	M3uaRoutesDTO toDTORoutes(M3uaRoutes m3uaRoute);
	void updateEntityFromRouteDTO(M3uaRoutesDTO m3uaRouteDTO, @MappingTarget M3uaRoutes entity);
}