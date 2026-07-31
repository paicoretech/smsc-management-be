package com.smsc.management.app.mno.service;

import com.smsc.management.app.errorcode.model.entity.ErrorCode;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.gateway.service.GatewaysService;
import com.smsc.management.app.mno.dto.OperatorMNODTO;
import com.smsc.management.app.mno.mapper.OperatorMnoMapper;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class OperatorMnoServiceTest {
    @Mock
    OperatorMnoRepository operatorMnoRepo;
    @Mock
    GatewaysRepository gatewaysRepo;
    @Mock
    Ss7GatewaysRepository ss7GatewaysRepo;
    @Mock
    ErrorCodeRepository errorCodeRepo;
    @Mock
    OperatorMnoMapper operatorMapper;
    @Mock
    GatewaysService gatewaysService;
    @InjectMocks
    OperatorMnoService operatorMnoService;

    @Test
    void getProviderExceptionTest() {
        when(operatorMnoRepo.findByEnabledTrue()).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = operatorMnoService.getOperators();
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    void creteDataIntegrityViolationExceptionTest() {
        when(operatorMnoRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        var operatorMnoDto = new OperatorMNODTO();
        assertThrows(DataIntegrityViolationException.class, () -> operatorMnoService.create(operatorMnoDto));
    }

    @Test
    void createAnyExceptionTest() {
        when(operatorMnoRepo.save(any())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = operatorMnoService.create(new OperatorMNODTO());
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    void deleteGwReferencedTest() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(gatewaysRepo.findByMnoIdAndEnabledNot(anyInt(), anyInt())).thenReturn(List.of(new Gateways()));
        when(ss7GatewaysRepo.findByMnoIdAndEnabledNot(anyInt(), anyInt())).thenReturn(List.of(new Ss7Gateways()));
        ApiResponse response = operatorMnoService.delete(1);
        assertNotNull(response);
        assertEquals(400, response.status());


        when(ss7GatewaysRepo.findByMnoIdAndEnabledNot(anyInt(), anyInt())).thenReturn(List.of());
        response = operatorMnoService.delete(1);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @Test
    void deleteErrorCodeMappingReferencedTest() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(errorCodeRepo.findByMnoId(anyInt())).thenReturn(List.of(new ErrorCode()));
        ApiResponse response = operatorMnoService.delete(1);
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @Test
    void deleteDataIntegrityViolationExceptionTest() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(operatorMnoRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        assertThrows(DataIntegrityViolationException.class, () -> operatorMnoService.delete(1));
    }

    @Test
    void deleteAnyExceptionTest() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(operatorMnoRepo.save(any())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = operatorMnoService.delete(1);
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    void deleteMnoNotFoundTest() {
        ApiResponse response = operatorMnoService.delete(1);
        assertNotNull(response);
        assertEquals(404, response.status());
    }

    @Test
    void updateMnoNotFoundTest() {
        ApiResponse response = operatorMnoService.update(1, new OperatorMNODTO());
        assertNotNull(response);
        assertEquals(404, response.status());
    }

    @Test
    void updateDataIntegrityViolationExceptionTest() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(operatorMnoRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        var operatorMnoDto = new OperatorMNODTO();
        assertThrows(DataIntegrityViolationException.class, () -> operatorMnoService.update(1, operatorMnoDto));
    }

    @Test
    void updateAnyExceptionTest() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(operatorMnoRepo.save(any())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = operatorMnoService.update(1, new OperatorMNODTO());
        assertNotNull(response);
        assertEquals(500, response.status());
    }

    @Test
    void updateWithGwServiceUpdateAllGwsByMnoFalse() {
        when(operatorMnoRepo.findByIdAndEnabledTrue(anyInt())).thenReturn(new OperatorMno());
        when(gatewaysService.updateAllGatewayByMno(anyInt())).thenReturn(false);
        ApiResponse response = operatorMnoService.update(1, new OperatorMNODTO());
        assertNotNull(response);
        assertEquals(200, response.status());
    }
}