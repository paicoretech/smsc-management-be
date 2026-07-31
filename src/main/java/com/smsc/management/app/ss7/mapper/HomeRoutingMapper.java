package com.smsc.management.app.ss7.mapper;

import com.smsc.management.app.ss7.dto.HomeRoutingCcMccMncDTO;
import com.smsc.management.app.ss7.dto.HomeRoutingDTO;
import com.smsc.management.app.ss7.model.entity.HomeRouting;
import com.smsc.management.app.ss7.model.entity.HomeRoutingCcMccMnc;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeRoutingMapper {

    // HomeRouting
    HomeRouting toEntity(HomeRoutingDTO homeRoutingDTO);
    HomeRoutingDTO toDTO(HomeRouting homeRoutingEntity);
    List<HomeRoutingDTO> toDTOList(List<HomeRouting> homeRoutingList);
    void updateEntityFromDTO(HomeRoutingDTO homeRoutingDTO, @MappingTarget HomeRouting entity);

    // HomeRoutingCcMccMnc
    HomeRoutingCcMccMnc toEntityCcMccMnc(HomeRoutingCcMccMncDTO ccMccMncDTO);
    HomeRoutingCcMccMncDTO toDTOCcMccMnc(HomeRoutingCcMccMnc ccMccMncEntity);
    List<HomeRoutingCcMccMncDTO> toDTOCcMccMncList(List<HomeRoutingCcMccMnc> ccMccMncList);
    void updateEntityFromCcMccMncDTO(HomeRoutingCcMccMncDTO ccMccMncDTO, @MappingTarget HomeRoutingCcMccMnc entity);
}
