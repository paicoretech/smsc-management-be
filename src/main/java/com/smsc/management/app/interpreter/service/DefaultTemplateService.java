package com.smsc.management.app.interpreter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import com.smsc.management.app.interpreter.model.repository.InterpreterRepository;
import com.smsc.management.exception.SmscBackendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.smsc.management.utils.Constants.INTERPRETER_DEFAULT_PATH;
import static com.smsc.management.utils.Constants.INTERPRETER_DEFAULT_TEMPLATE;
import static com.smsc.management.utils.Constants.INTERPRETER_KEY_TEMPLATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultTemplateService {
    private final GatewaysRepository gatewaysRepository;
    private final InterpreterRepository interpreterRepository;

    public void createDefaultTemplate(int networkId) {
        try {
            Gateways gateways = gatewaysRepository.findByNetworkIdAndProtocol(networkId, "HTTP");

            if (Objects.isNull(gateways)) {
                throw new SmscBackendException("Gateway not found");
            }

            List<Interpreter> interpreters = new ArrayList<>();
            JsonNode templates = Converter.stringToObject(INTERPRETER_DEFAULT_TEMPLATE, JsonNode.class);
            JsonNode defaultPath = Converter.stringToObject(INTERPRETER_DEFAULT_PATH, JsonNode.class);
            String[] keysTemplate = INTERPRETER_KEY_TEMPLATE.split(",");

            for (String key : keysTemplate) {
                Interpreter interpreter = new Interpreter();

                String[] parts = key.split("\\|");
                String eventType = parts[0];
                String direction = parts[1];

                if (defaultPath.has(key)) {
                    interpreter.setPath(defaultPath.get(key).asText());
                }
                interpreter.setEventType(eventType);
                interpreter.setDirection(direction);
                interpreter.setBodyType("JSON");
                interpreter.setUseProxy(false);
                interpreter.setGatewayId(networkId);
                interpreter.setTemplate(templates.get(key).toString());
                interpreter.setDefaultTemplate(true);

                interpreters.add(interpreter);
            }

            interpreterRepository.saveAll(interpreters);
        } catch (Exception e) {
            log.error("Error creating default template for networkId {}", networkId, e);
        }
    }
}
