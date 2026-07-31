package com.smsc.management.app.errorcode.service;

import com.smsc.management.app.errorcode.dto.ErrorCodeMappingDTO;
import com.smsc.management.app.errorcode.mapper.ErrorCodeMappingMapper;
import com.smsc.management.app.errorcode.model.entity.ErrorCodeMapping;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeMappingRepository;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.UtilsBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ErrorCodeMappingServiceTest {

    @Mock
    ErrorCodeMappingRepository errorCodeMappingRepo;

    @Mock
    ErrorCodeRepository errorCodeRepo;

    @Mock
    ErrorCodeMappingMapper errorCodeMappingMapper;

    @Mock
    UtilsBase utils;

    @InjectMocks
    ErrorCodeMappingService errorCodeMappingService;

    @Test
    void getErrorCodeMappingAnyExceptionTest() {
        when(errorCodeMappingRepo.fetchErrorCodeMappings()).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeMappingService.getErrorCodeMapping();
        assertEquals(500, response.status());
    }

    @Test
    void createDataIntegrityExceptionTest() {
        ErrorCodeMappingDTO errorCodeMappingDTO = new ErrorCodeMappingDTO();
        when(errorCodeMappingMapper.toEntity(errorCodeMappingDTO)).thenReturn(new ErrorCodeMapping());
        when(errorCodeMappingRepo.save(any())).thenThrow(new DataIntegrityViolationException(""));
        assertThrows(DataIntegrityViolationException.class, () -> errorCodeMappingService.create(errorCodeMappingDTO));
    }

    @Test
    void createAnyExceptionTest() {
        when(errorCodeMappingMapper.toEntity(any())).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeMappingService.create(any());
        assertEquals(500, response.status());
    }

    @Test
    void updateNotFoundExceptionTest() {
        when(errorCodeMappingRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = errorCodeMappingService.update(1, new ErrorCodeMappingDTO());
        assertEquals(404, response.status());
    }

    @Test
    void updateDataIntegrityExceptionTest() {
        when(errorCodeMappingRepo.findById(anyInt())).thenReturn(new ErrorCodeMapping());
        when(errorCodeMappingRepo.save(any())).thenThrow(new DataIntegrityViolationException(""));
        ErrorCodeMappingDTO errorCodeMappingDto = new ErrorCodeMappingDTO();
        assertThrows(DataIntegrityViolationException.class, () -> errorCodeMappingService.update(1, errorCodeMappingDto));
    }

    @Test
    void updateAnyExceptionTest() {
        when(errorCodeMappingRepo.findById(5)).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = errorCodeMappingService.update(5, new ErrorCodeMappingDTO());
        assertEquals(500, response.status());
    }

    @Test
    void deleteNotFoundExceptionTest() {
        when(errorCodeMappingRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = errorCodeMappingService.delete(1);
        assertEquals(404, response.status());
    }

    @Test
    void deleteDataIntegrityExceptionTest() {
        ErrorCodeMapping errorCodeMapping = new ErrorCodeMapping();
        when(errorCodeMappingRepo.findById(1)).thenReturn(errorCodeMapping);
        doThrow(new DataIntegrityViolationException("")).when(errorCodeMappingRepo).delete(any());
        assertThrows(DataIntegrityViolationException.class, () -> errorCodeMappingService.delete(1));
    }

    @Test
    void deleteAnyExceptionTest() {
        when(errorCodeMappingRepo.findById(5)).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeMappingService.delete(5);
        assertEquals(500, response.status());
    }

    @Test
    void socketAndRedisActionCatchPartTest() {
        when(errorCodeMappingRepo.findByMnoId(1)).thenThrow(new RuntimeException());
        boolean result = errorCodeMappingService.socketAndRedisAction(1);
        assertFalse(result);
    }
}
