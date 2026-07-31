package com.smsc.management.app.errorcode.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smsc.management.app.errorcode.dto.ErrorCodeMappingDTO;
import com.smsc.management.app.errorcode.model.entity.ErrorCodeMapping;

@Mapper(componentModel = "spring")
public interface ErrorCodeMappingMapper {
	ErrorCodeMapping toEntity(ErrorCodeMappingDTO errorCodeMappignDTO);
	ErrorCodeMappingDTO toDTO(ErrorCodeMapping errorCodeMappingEntity);
	List<ErrorCodeMappingDTO> toDTO(List<ErrorCodeMapping> errorCodes);
	
	void updateToEntity(ErrorCodeMappingDTO dto, @MappingTarget ErrorCodeMapping entity);
}
