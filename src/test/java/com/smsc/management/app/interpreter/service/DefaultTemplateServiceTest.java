package com.smsc.management.app.interpreter.service;

import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.interpreter.model.repository.InterpreterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class DefaultTemplateServiceTest {

    @Mock
    private GatewaysRepository gatewaysRepository;

    @Mock
    private InterpreterRepository interpreterRepository;

    @InjectMocks
    private DefaultTemplateService defaultTemplateService;

    @Test
    @DisplayName("Create default interpreter template when gateway not found")
    void createDefaultTemplateWhenGatewayNotFoundThenThrowExceptionOccur() {
        defaultTemplateService.createDefaultTemplate(1);
        Mockito.verify(interpreterRepository, Mockito.never()).saveAll(Mockito.anyList());
    }
}