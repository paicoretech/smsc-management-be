package com.smsc.management.app.settings.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.smsc.management.app.settings.dto.CommonVariablesDTO;
import com.smsc.management.app.settings.model.entity.CommonVariables;

@Mapper(componentModel = "spring")
public interface CommonVariablesMapper {
	CommonVariables toEntity(CommonVariablesDTO commonVar);
	CommonVariablesDTO toDTO(CommonVariables commonVar);
	
	List<CommonVariables> toEntity(List<CommonVariablesDTO> commonVar);
	List<CommonVariablesDTO> toDTO(List<CommonVariables> commonVar);
}
