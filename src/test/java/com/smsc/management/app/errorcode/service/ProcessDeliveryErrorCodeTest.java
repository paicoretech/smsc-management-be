package com.smsc.management.app.errorcode.service;

import com.smsc.management.app.errorcode.dto.DeliveryErrorCodeDTO;
import com.smsc.management.app.errorcode.mapper.DeliveryErrorCodeMapper;
import com.smsc.management.app.errorcode.model.entity.DeliveryErrorCode;
import com.smsc.management.app.errorcode.model.repository.DeliveryErrorCodeRepository;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessDeliveryErrorCodeTest {

    @Mock
    DeliveryErrorCodeRepository deliveryErrorCodeRepo;

    @Mock
    DeliveryErrorCodeMapper deliveryErrorCodeMapper;

    @InjectMocks
    ProcessDeliveryErrorCode processDeliveryErrorCode;

    @Test
    void getDeliveryErrorCodeAnyExceptionTest() {
        when(deliveryErrorCodeRepo.findAll()).thenThrow(new RuntimeException());
        ApiResponse response = processDeliveryErrorCode.getDeliveryErrorCode();
        assertEquals(500, response.status());
    }

    @Test
    void createDataIntegrityExceptionTest() {
        DeliveryErrorCodeDTO deliveryErrorCodeDTO = new DeliveryErrorCodeDTO();
        when(deliveryErrorCodeMapper.toEntity(deliveryErrorCodeDTO)).thenReturn(new DeliveryErrorCode());
        when(deliveryErrorCodeRepo.save(any())).thenThrow(new DataIntegrityViolationException(""));
        assertThrows(DataIntegrityViolationException.class, () -> processDeliveryErrorCode.create(deliveryErrorCodeDTO));
    }

    @Test
    void createAnyExceptionTest() {
        when(deliveryErrorCodeMapper.toEntity(any())).thenThrow(new RuntimeException());
        ApiResponse response = processDeliveryErrorCode.create(any());
        assertEquals(500, response.status());
    }

    @Test
    void updateNotFoundExceptionTest() {
        when(deliveryErrorCodeRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = processDeliveryErrorCode.update(1, new DeliveryErrorCodeDTO());
        assertEquals(404, response.status());
    }

    @Test
    void updateDataIntegrityExceptionTest() {
        when(deliveryErrorCodeRepo.findById(anyInt())).thenReturn(new DeliveryErrorCode());
        when(deliveryErrorCodeRepo.save(any())).thenThrow(new DataIntegrityViolationException(""));
        DeliveryErrorCodeDTO deliveryErrorCodeDTO = new DeliveryErrorCodeDTO();
        assertThrows(DataIntegrityViolationException.class, () -> processDeliveryErrorCode.update(1, deliveryErrorCodeDTO));
    }

    @Test
    void updateAnyExceptionTest() {
        when(deliveryErrorCodeRepo.findById(1)).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = processDeliveryErrorCode.update(1, new DeliveryErrorCodeDTO());
        assertEquals(500, response.status());
    }

    @Test
    void deleteNotFoundExceptionTest() {
        when(deliveryErrorCodeRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = processDeliveryErrorCode.delete(1);
        assertEquals(404, response.status());
    }

    @Test
    void deleteDataIntegrityExceptionTest() {
        DeliveryErrorCode deliveryErrorCode = new DeliveryErrorCode();
        when(deliveryErrorCodeRepo.findById(1)).thenReturn(deliveryErrorCode);
        doThrow(new DataIntegrityViolationException("")).when(deliveryErrorCodeRepo).delete(any());
        assertThrows(DataIntegrityViolationException.class, () -> processDeliveryErrorCode.delete(1));
    }

    @Test
    void deleteAnyExceptionTest() {
        when(deliveryErrorCodeRepo.findById(1)).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = processDeliveryErrorCode.delete(1);
        assertEquals(500, response.status());
    }
}
