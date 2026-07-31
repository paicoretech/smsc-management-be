package com.smsc.management.app.sip.mapper;

import com.smsc.management.app.sip.dto.RedisSipDTO;
import com.smsc.management.app.sip.dto.SipGatewaysDTO;
import com.smsc.management.app.sip.model.entity.SipGateways;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SipGatewaysMapper {
    SipGateways toEntity(SipGatewaysDTO dto);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "protocol", ignore = true)
    void updateEntityFromDTO(SipGatewaysDTO dto, @MappingTarget SipGateways entity);

    SipGatewaysDTO toDTO(SipGateways entity);
    RedisSipDTO toRedisDTO(SipGateways entity);
}
