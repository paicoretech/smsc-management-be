package com.smsc.management.app.provider.mapper;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.utils.Converter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.provider.dto.ParseServiceProviderDTO;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.app.provider.dto.ServiceProviderDTO;
import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import com.smsc.management.app.provider.model.entity.ServiceProvider;

@Mapper(componentModel = "spring")
public interface ServiceProviderMapper {
	ServiceProvider toEntity(ServiceProviderDTO serviceProviderDTO);
	ParseServiceProviderDTO toDTO(ServiceProvider serviceProviderEntity);
	List<ParseServiceProviderDTO> toDTO(List<ServiceProvider> serviceProviders);

	@Mapping(target = "customParameters", expression = "java(parseCustomParameters(serviceProviderEntity.getCustomParameters()))")
	RedisServiceProviderDTO toServiceProviderDTO(ServiceProvider serviceProviderEntity);

	void updateEntityFromDTO(ServiceProviderDTO dto, @MappingTarget ServiceProvider entity);
	List<CallbackHeaderHttpDTO> toDTOCallbackHeader(List<CallbackHeaderHttp> headers);
	CallbackHeaderHttp toEntityCallbackHeader(CallbackHeaderHttpDTO headerDTO);

	default Map<String, String> parseCustomParameters(String json) {
		if (json == null || json.isBlank()) {
			return null;
		}
		try {
			return Converter.stringToObject(json, new TypeReference<>() {});
		} catch (Exception e) {
			return null;
		}
	}
}
