package com.smsc.management.app.ss7.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.smsc.management.app.ss7.dto.SccpAddressesDTO;
import com.smsc.management.app.ss7.dto.SccpDTO;
import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.dto.SccpRemoteResourcesDTO;
import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.dto.SccpServiceAccessPointsDTO;
import com.smsc.management.app.ss7.model.entity.Sccp;
import com.smsc.management.app.ss7.model.entity.SccpAddresses;
import com.smsc.management.app.ss7.model.entity.SccpLongMessageRules;
import com.smsc.management.app.ss7.model.entity.SccpMtp3Destinations;
import com.smsc.management.app.ss7.model.entity.SccpRemoteResources;
import com.smsc.management.app.ss7.model.entity.SccpRules;
import com.smsc.management.app.ss7.model.entity.SccpServiceAccessPoints;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SccpMapper {
	Sccp toEntity(SccpDTO sccpDto);
	SccpDTO toDTO(Sccp sccpEntity);
	List<Sccp> toEntity(List<SccpDTO> sccpDtoList);
	List<SccpDTO> toDTO(List<Sccp> sccpEntity);
	void updateEntityFromSCCPDTO(SccpDTO sccpDto, @MappingTarget Sccp entity);
	
	SccpRemoteResources toEntityRemoteResources(SccpRemoteResourcesDTO sccpRemoteResource);
	SccpRemoteResourcesDTO toDTORemoteResources(SccpRemoteResources sccpRemoteResourceEntity);
	List<SccpRemoteResourcesDTO> toDTORemoteResources(List<SccpRemoteResources> sccpRemoteResourcesEntity);
	void updateEntityFromRemoteResourcesDTO(SccpRemoteResourcesDTO sccpRemoteResource, @MappingTarget SccpRemoteResources entity);
	
	SccpServiceAccessPoints toEntitySap(SccpServiceAccessPointsDTO sccpSapDTO);
	SccpServiceAccessPointsDTO toDTOSap(SccpServiceAccessPoints sccpSapEntity);
	List<SccpServiceAccessPointsDTO> toDTOSap(List<SccpServiceAccessPoints> sccpSapEntity);
	void updateEntityFromSapDTO(SccpServiceAccessPointsDTO sccpSapDTO, @MappingTarget SccpServiceAccessPoints entity);
	
	SccpMtp3Destinations toEntityMTP3(SccpMtp3DestinationsDTO sccpMtpDest);
	SccpMtp3DestinationsDTO toDTOMTP3(SccpMtp3Destinations sccpMtpDestEntity);
	void updateEntityFromMtp3DTO(SccpMtp3DestinationsDTO sccpMtpDest, @MappingTarget SccpMtp3Destinations entity);
	
	SccpAddresses toEntityAddress(SccpAddressesDTO addressDTO);
	SccpAddressesDTO toDTOAddress(SccpAddresses addressEntity);
	List<SccpAddressesDTO> toDTOAddress(List<SccpAddresses> addressEntity);
	void updateEntityFromAddressDTO(SccpAddressesDTO addressDTO, @MappingTarget SccpAddresses entity);
	
	SccpRules toEntityRules(SccpRulesDTO sccpRulesDTO);
	SccpRulesDTO toDTORules(SccpRules sccpRulesEntity);
	void updateEntityFromRulesDTO(SccpRulesDTO sccpRulesDTO, @MappingTarget SccpRules entity);

	SccpLongMessageRules toEntityLongMessageRules(SccpLongMessageRulesDTO longMessageRulesDTO);
	SccpLongMessageRulesDTO toDTOLongMessageRules(SccpLongMessageRules longMessageRulesEntity);
	void updateEntityFromLongMessageRulesDTO(SccpLongMessageRulesDTO longMessageRulesDTO, @MappingTarget SccpLongMessageRules entity);
}
