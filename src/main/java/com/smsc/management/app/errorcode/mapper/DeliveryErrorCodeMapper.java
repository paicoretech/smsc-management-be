package com.smsc.management.app.errorcode.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smsc.management.app.errorcode.dto.DeliveryErrorCodeDTO;
import com.smsc.management.app.errorcode.model.entity.DeliveryErrorCode;

@Mapper(componentModel = "spring")
public interface DeliveryErrorCodeMapper {
	DeliveryErrorCode toEntity(DeliveryErrorCodeDTO serverErrorCodeDTO);
	DeliveryErrorCodeDTO toDTO(DeliveryErrorCode serverErrorCodeEntity);
	List<DeliveryErrorCodeDTO> toDTO(List<DeliveryErrorCode> serverErrorCodes);
	void updateToEntity(DeliveryErrorCodeDTO dto, @MappingTarget DeliveryErrorCode entity);
}
