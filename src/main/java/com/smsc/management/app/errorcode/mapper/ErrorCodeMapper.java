package com.smsc.management.app.errorcode.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smsc.management.app.errorcode.dto.ErrorCodeDTO;
import com.smsc.management.app.errorcode.model.entity.ErrorCode;

@Mapper(componentModel = "spring")
public interface ErrorCodeMapper {
	ErrorCode toEntity(ErrorCodeDTO errorCodeDTO);
	ErrorCodeDTO toDTO(ErrorCode errorCodeEntity);
	List<ErrorCodeDTO> toListDTO(List<ErrorCode> errorCodes);
	
	void updateToEntity(ErrorCodeDTO dto, @MappingTarget ErrorCode entity);
}
