package com.smsc.management.app.settings.service;

import com.smsc.management.app.credit.custom.HandlerCreditByServiceProvider;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.settings.dto.CommonVariablesDTO;
import com.smsc.management.app.settings.mapper.CommonVariablesMapper;
import com.smsc.management.app.settings.model.entity.CommonVariables;
import com.smsc.management.app.settings.model.repository.CommonVariablesRepository;
import com.smsc.management.init.LoadSpAndGwInRedis;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class CommonVariableServiceTest {

    @Mock
    private CommonVariablesRepository commonVarRepo;

    @Mock
    private GatewaysRepository gatewaysRepository;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private CommonVariablesMapper commonVarMapper;

    @Mock
    private HandlerCreditByServiceProvider handlerCreditBySp;

    @Mock
    private LoadSpAndGwInRedis loadSp;

    @InjectMocks
    private CommonVariableService commonVariableService;


    @Test
    void getCommonVariableWithErrorTest() {
        Mockito.when(this.commonVarRepo.findAll()).thenThrow(NullPointerException.class);
        var response = commonVariableService.get();
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());
    }

    @Test
    void massiveUpdateTestWithErrorKey() {
        var commonVariablesFake = new CommonVariablesDTO();
        commonVariablesFake.setKey("SMSC_FAKE");
        commonVariablesFake.setDataType("boolean");
        commonVariablesFake.setValue("false");
        var listToUpdate = List.of(commonVariablesFake);
        var response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(400, response.status());
        assertEquals("error", response.message());
    }

    @Test
    void massiveUpdateBooleanTest() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariablesLocalCharging =
                getMockOfCommonVariablesDTO("USE_LOCAL_CHARGING", "boolean", "hello");
        listToUpdate = List.of(commonVariablesLocalCharging);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("USE_LOCAL_CHARGING", "boolean", "false"));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());

        commonVariablesLocalCharging.setValue("true");
        listToUpdate = List.of(commonVariablesLocalCharging);
        Mockito.when(this.commonVarRepo.saveAll(anyList()))
                .thenReturn(List.of(getMockOfCommonVariableEntity("USE_LOCAL_CHARGING", "boolean", "false")));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    void massiveUpdateIntTest() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariable = getMockOfCommonVariablesDTO("KEY_INT", "int", "hello");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("KEY_INT", "int", "10"));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());

        commonVariable.setValue("20");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.saveAll(anyList()))
                .thenReturn(List.of(getMockOfCommonVariableEntity("KEY_INT", "int", "20")));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    void massiveUpdateJsonTest() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariable = getMockOfCommonVariablesDTO("SMSC_ACCOUNT_SETTINGS", "json", "\"stringValue\"");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}"));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());

        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "mewType", "{\"max_password_length\":18,\"max_system_id_length\":20}"));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());


        commonVariable.setValue("20");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}"));
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());
    }


    @Test
    void massiveUpdateAccountSpMaxSystemLength() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariable = getMockOfCommonVariablesDTO("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}"));

        Mockito.when(serviceProviderRepository.countBySystemIdLengthGreaterThan(anyInt())).thenReturn(100);
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());
        assertEquals("Error to update parameter (100 Service provider accounts were found with system id longer than the maximum length you want to configure.)", response.comment());
    }

    @Test
    void massiveUpdateAccountGwMaxSystemLength() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariable = getMockOfCommonVariablesDTO("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}"));

        Mockito.when(gatewaysRepository.countBySystemIdLengthGreaterThan(anyInt())).thenReturn(100);
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());
        assertEquals("Error to update parameter (100 Gateways accounts were found with system id longer than the maximum length you want to configure.)", response.comment());
    }


    @Test
    void massiveUpdateAccountSpMaxPasswordLength() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariable = getMockOfCommonVariablesDTO("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}"));

        Mockito.when(serviceProviderRepository.countByPasswordLengthGreaterThan(anyInt())).thenReturn(100);
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());
        assertEquals("Error to update parameter (100 Service provider accounts were found with passwords longer than the maximum length you want to configure.)", response.comment());
    }

    @Test
    void massiveUpdateAccountGwMaxPasswordLength() {
        List<CommonVariablesDTO> listToUpdate;
        ApiResponse response;
        var commonVariable = getMockOfCommonVariablesDTO("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}");
        listToUpdate = List.of(commonVariable);
        Mockito.when(this.commonVarRepo.findByKey(anyString()))
                .thenReturn(getMockOfCommonVariableEntity("SMSC_ACCOUNT_SETTINGS", "json", "{\"max_password_length\":18,\"max_system_id_length\":20}"));

        Mockito.when(gatewaysRepository.countByPasswordLengthGreaterThan(anyInt())).thenReturn(100);
        response = commonVariableService.massiveUpdate(listToUpdate);
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("error", response.message());
        assertEquals("Error to update parameter (100 Gateways accounts were found with passwords longer than the maximum length you want to configure.)", response.comment());
    }

    private CommonVariables getMockOfCommonVariableEntity(String key, String dataType, String value) {
        CommonVariables commonVariables = new CommonVariables();
        commonVariables.setKey(key);
        commonVariables.setDataType(dataType);
        commonVariables.setValue(value);
        return commonVariables;
    }


    private CommonVariablesDTO getMockOfCommonVariablesDTO(String key, String dataType, String value) {
        CommonVariablesDTO commonVariables = new CommonVariablesDTO();
        commonVariables.setKey(key);
        commonVariables.setDataType(dataType);
        commonVariables.setValue(value);
        return commonVariables;
    }

}