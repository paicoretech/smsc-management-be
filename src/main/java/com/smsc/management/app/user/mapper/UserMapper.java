package com.smsc.management.app.user.mapper;

import com.smsc.management.app.user.dto.UsersDTO;
import com.smsc.management.app.user.model.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    Users toEntity(UsersDTO usersDTO);
    @Mapping(source = "username", target = "userName")
    UsersDTO toDTO(Users users);
    void updateEntityFromDTO(UsersDTO dto, @MappingTarget Users entity);
}
