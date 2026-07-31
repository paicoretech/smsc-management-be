package com.smsc.management.app.interpreter.service;

import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.gateway.service.GatewaysService;
import com.smsc.management.app.headers.dto.CallbackHeaderHttpDTO;
import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import com.smsc.management.app.headers.model.repository.CallbackHeaderHttpRepository;
import com.smsc.management.app.interpreter.dto.HttpGatewaysDTO;
import com.smsc.management.app.interpreter.dto.InterpreterDTO;
import com.smsc.management.app.interpreter.mapper.InterpreterMapper;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import com.smsc.management.app.interpreter.model.repository.InterpreterRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.ResponseMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.smsc.management.utils.Constants.DELETED_ENABLED_STATUS;

@Slf4j
@Service
@RequiredArgsConstructor
public class    InterpreterService {
    private final GatewaysRepository gatewaysRepository;
    private final CallbackHeaderHttpRepository callbackHeaderRepo;
    private final InterpreterRepository interpreterRepository;
    private final InterpreterMapper interpreterMapper;
    private final GatewaysService gatewaysService;

    public ApiResponse getInterpreterSettings() {
        try {
            List<Interpreter> entities = interpreterRepository.getAllInterpreters();
            List<InterpreterDTO> interpreters = new ArrayList<>();

            for (Interpreter interpreter : entities) {
                List<CallbackHeaderHttp> headers = callbackHeaderRepo.findByInterpreterId(interpreter.getId());
                List<CallbackHeaderHttpDTO> headersDTO = interpreterMapper.toDTOCallbackHeader(headers);
                InterpreterDTO interpreterDTO = interpreterMapper.toDto(interpreter);
                interpreterDTO.setGatewayName(interpreter.getGateways().getName());
                interpreterDTO.setCallbackHeadersHttp(headersDTO);
                interpreters.add(interpreterDTO);
            }
            return ResponseMapping.successMessage("Get interpreter request success", interpreters);
        } catch (Exception e) {
            log.error("Error to get interpreter settings: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("error to get interpreter settings", e);
        }
    }

    public ApiResponse updateInterpreter(int id, InterpreterDTO interpreter) {
        try {
            Interpreter currentInterpreter = interpreterRepository.findById(id);
            if (Objects.nonNull(currentInterpreter)) {
                interpreter.setId(id);

                if (gatewaysRepository.existsByEnabledNotAndNetworkIdAndProtocol(DELETED_ENABLED_STATUS, currentInterpreter.getGatewayId(), "HTTP")) {
                    currentInterpreter.setTemplate(interpreter.getTemplate());
                    currentInterpreter.setUseProxy(interpreter.isUseProxy());
                    currentInterpreter.setDefaultTemplate(interpreter.isDefaultTemplate());
                    if (!interpreter.isDefaultTemplate()) {
                        this.validatePath(interpreter, currentInterpreter);
                        currentInterpreter.setPath(interpreter.getPath());
                        currentInterpreter.setBodyType(interpreter.getBodyType());
                    }

                    List<CallbackHeaderHttpDTO> callHeaders = interpreter.getCallbackHeadersHttp();
                    updateCallbacks(currentInterpreter.getId(), callHeaders);

                    var result  = interpreterRepository.save(currentInterpreter);

                    gatewaysService.onlyToLoadInitInRedisAndSocket(result.getGatewayId());
                    return ResponseMapping.successMessage("Interpreter updated successfully", interpreterMapper.toDto(result));
                }

                return ResponseMapping.errorMessage("Gateway HTTP id " + currentInterpreter.getGatewayId() + " was not found");
            }

            return ResponseMapping.errorMessage("Not found Interpreter id " + id);
        } catch (Exception e) {
            log.error("Error to update interpreter: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Error to update interpreter", e);
        }
    }

    private void updateCallbacks(int interpreterId, List<CallbackHeaderHttpDTO> callHeaders) {
        callbackHeaderRepo.deleteAllByInterpreterId(interpreterId);

        for (CallbackHeaderHttpDTO header : callHeaders) {
            CallbackHeaderHttp headerEntity = interpreterMapper.toEntityCallbackHeader(header);
            headerEntity.setInterpreterId(interpreterId);
            callbackHeaderRepo.save(headerEntity);
        }
    }

    public ApiResponse getHttpGateways() {
        try {
            List<HttpGatewaysDTO> gateways = gatewaysRepository.findHttpGatewaysList();
            return ResponseMapping.successMessage("Get http gateways successful", gateways);
        } catch (Exception e) {
            log.error("Error to get http gateways list: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Error to get http gateways", e);
        }
    }

    private void validatePath(InterpreterDTO interpreter, Interpreter currentInterpreter) {
        String path = interpreter.getPath();
        boolean isInput = "input".equalsIgnoreCase(currentInterpreter.getDirection());
        boolean isMessageOrDlr = Arrays.asList("message", "dlr").contains(currentInterpreter.getEventType());

        if (isInput && isMessageOrDlr) {
            if (Optional.ofNullable(path).orElse("").isEmpty()) {
                throw new IllegalArgumentException("path should not be empty");
            }

            // validate unique path between gateway when is not default configuration
            boolean interpreterExists = interpreterRepository
                    .existsByPathAndGatewayIdNotAndDefaultTemplateIsFalse(path, currentInterpreter.getGatewayId());
            if (interpreterExists) {
                throw new IllegalArgumentException("path is duplicated");
            }
        }
    }
}
