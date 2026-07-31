package com.smsc.management.app.interpreter.mapper;

import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import com.smsc.management.app.interpreter.dto.InterpreterDTO;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InterpreterMapper {
    Interpreter toEntity(InterpreterDTO interpreter);
    InterpreterDTO toDto(Interpreter interpreter);
    List<InterpreterDTO> toDtoList(List<Interpreter> interpreters);

    void updateToEntity(InterpreterDTO dto, @MappingTarget Interpreter entity);
    List<CallbackHeaderHttpDTO> toDTOCallbackHeader(List<CallbackHeaderHttp> headers);
    CallbackHeaderHttp toEntityCallbackHeader(CallbackHeaderHttpDTO headerDTO);
}
