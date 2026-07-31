package com.smsc.management.app.mno.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.smsc.management.app.mno.dto.OperatorMNODTO;
import com.smsc.management.app.mno.model.entity.OperatorMno;

@Mapper(componentModel = "spring")
public interface OperatorMnoMapper {
	OperatorMno toEntity(OperatorMNODTO operatorMNODTO);
	OperatorMNODTO toDTO(OperatorMno operatorMNOEntity);
	List<OperatorMNODTO> toDTO(List<OperatorMno> operatorMNOs);
}
