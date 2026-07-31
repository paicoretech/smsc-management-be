package com.smsc.management.app.credit.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.smsc.management.app.credit.dto.CreditSalesHistoryDTO;
import com.smsc.management.app.credit.model.entity.CreditSalesHistory;

@Mapper(componentModel = "spring")
public interface CreditSalesMapper {
	CreditSalesHistoryDTO toDTO(CreditSalesHistory creditSalesEntity);
	
	List<CreditSalesHistoryDTO> toListDTO(List<CreditSalesHistory> creditSalesEntity);
}
