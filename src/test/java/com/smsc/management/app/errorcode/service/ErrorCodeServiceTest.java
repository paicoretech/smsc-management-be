package com.smsc.management.app.errorcode.service;

import com.smsc.management.app.errorcode.dto.ErrorCodeDTO;
import com.smsc.management.app.errorcode.mapper.ErrorCodeMapper;
import com.smsc.management.app.errorcode.model.entity.ErrorCode;
import com.smsc.management.app.errorcode.model.repository.ErrorCodeRepository;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ErrorCodeServiceTest {

    @Mock
    ErrorCodeRepository errorCodeRepo;

    @Mock
    ErrorCodeMapper errorCodeMapper;

    @InjectMocks
    ErrorCodeService errorCodeService;

    @Test
    void getErrorCodeListAnyExceptionTest() {
        when(errorCodeRepo.fetchErrorCode()).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeService.getErrorCodeList();
        assertEquals(500, response.status());
    }

    @Test
    void getErrorCodeListByMnoAnyExceptionTest() {
        when(errorCodeRepo.findByMnoId(anyInt())).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeService.getErrorCodeListByMNO(anyInt());
        assertEquals(500, response.status());
    }

    @Test
    void createDataIntegrityExceptionTest() {
        ErrorCodeDTO errorCodeDTO = new ErrorCodeDTO();
        when(errorCodeMapper.toEntity(errorCodeDTO)).thenReturn(new ErrorCode());
        when(errorCodeRepo.save(any())).thenThrow(new DataIntegrityViolationException(""));
        assertThrows(DataIntegrityViolationException.class, () -> errorCodeService.create(errorCodeDTO));
    }

    @Test
    void createAnyExceptionTest() {
        when(errorCodeMapper.toEntity(any())).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeService.create(any());
        assertEquals(500, response.status());
    }

    @Test
    void updateNotFoundExceptionTest() {
        when(errorCodeRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = errorCodeService.update(1, new ErrorCodeDTO());
        assertEquals(404, response.status());
    }

    @Test
    void updateDataIntegrityExceptionTest() {
        when(errorCodeRepo.findById(anyInt())).thenReturn(new ErrorCode());
        when(errorCodeRepo.save(any())).thenThrow(new DataIntegrityViolationException(""));
        ErrorCodeDTO errorCodeDTO = new ErrorCodeDTO();
        assertThrows(DataIntegrityViolationException.class, () -> errorCodeService.update(1, errorCodeDTO));
    }

    @Test
    void updateAnyExceptionTest() {
        ApiResponse response = errorCodeService.update(anyInt(), any());
        assertEquals(500, response.status());
    }

    @Test
    void deleteNotFoundExceptionTest() {
        when(errorCodeRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = errorCodeService.delete(1);
        assertEquals(404, response.status());
    }

    @Test
    void deleteDataIntegrityExceptionTest() {
        ErrorCode errorCode = new ErrorCode();
        when(errorCodeRepo.findById(anyInt())).thenReturn(errorCode);
        doThrow(new DataIntegrityViolationException("")).when(errorCodeRepo).delete(errorCode);
        assertThrows(DataIntegrityViolationException.class, () -> errorCodeService.delete(1));
    }

    @Test
    void deleteAnyExceptionTest() {
        when(errorCodeRepo.findById(any())).thenThrow(new RuntimeException());
        ApiResponse response = errorCodeService.delete(anyInt());
        assertEquals(500, response.status());
    }
}
