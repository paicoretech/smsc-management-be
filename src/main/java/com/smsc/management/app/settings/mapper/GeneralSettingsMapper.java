package com.smsc.management.app.settings.mapper;

import org.mapstruct.Mapper;

import com.smsc.management.app.settings.dto.GeneralSettingsSmppHttpDTO;
import com.smsc.management.app.settings.dto.GeneralSmscRetryDTO;
import com.smsc.management.app.settings.model.entity.GeneralSettingsSmppHttp;
import com.smsc.management.app.settings.model.entity.GeneralSmscRetry;

@Mapper(componentModel = "spring")
public interface GeneralSettingsMapper {
	GeneralSettingsSmppHttp toEntity(GeneralSettingsSmppHttpDTO generalSettingDTO);
	GeneralSettingsSmppHttpDTO toDTO(GeneralSettingsSmppHttp generalSettingEntity);
	
	GeneralSmscRetry toEntitySmscRetry(GeneralSmscRetryDTO smscRetryDTO);
	GeneralSmscRetryDTO toDTOSmscRetry(GeneralSmscRetry smscRetryEntity);
}
