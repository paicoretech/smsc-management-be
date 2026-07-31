package com.smsc.management.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseMappingTest {

    @Test
    @DisplayName("Should return ApiResponse with status 400 and error message when calling errorMessage"  )
    void shouldReturnApiResponseWithStatus400AndErrorMessageWhenCallingErrorMessage() {
        String errorMessage = "Test error message";
        ApiResponse response = ResponseMapping.errorMessage(errorMessage);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should return ApiResponse with status 200 and success message when calling successMessage"  )
    void shouldReturnApiResponseWithStatus200AndSuccessMessageWhenCallingSuccessMessage() {
        String successMessage = "Test success message";
        Object data = new Object();
        ApiResponse response = ResponseMapping.successMessage(successMessage, data);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should return ApiResponse with status 500 and error message when calling exceptionMessage"  )
    void shouldReturnApiResponseWithStatus500AndErrorMessageWhenCallingExceptionMessage() {
        String exceptionMessage = "Test exception message";
        Exception exception = new Exception("Test cause");
        ApiResponse response = ResponseMapping.exceptionMessage(exceptionMessage, exception);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should return ApiResponse with status 404 and error message when calling errorMessageNoFound"  )
    void shouldReturnApiResponseWithNotFoundMessageWhenCallingSuccessMessage() {
        String notFoundMessage = "No found message";
        ApiResponse response = ResponseMapping.errorMessageNoFound(notFoundMessage);
        assertNotNull(response);
    }

    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<ResponseMapping> constructor = ResponseMapping.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    @DisplayName("exceptionConstrainMessage response with a real message then do it successfully")
    void exceptionConstrainMessageWhenViolateUniqueConstrainMessageTheDoItSuccessfully() {
        String inputCause = "could not execute statement [ERROR: duplicate key value violates unique constraint \"ukftxjkkockmiwbc4l4im7rpuok\"\n" +
                "  Detail: Key (ip, port)=(127.0.0.1, 7778) already exists.] [insert into smpp_server (created_at,created_by_id,enabled,ip,is_default,name,port,processor_degree,queue_capacity,status,transaction_timer,updated_at,updated_by_id,wait_for_bind,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)]; SQL [insert into smpp_server (created_at,created_by_id,enabled,ip,is_default,name,port,processor_degree,queue_capacity,status,transaction_timer,updated_at,updated_by_id,wait_for_bind,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)]; constraint [ukftxjkkockmiwbc4l4im7rpuok]";

        String outputCommentError = "New smpp server error test (ERROR: duplicate key value violates unique constraint \"ukftxjkkockmiwbc4l4im7rpuok\"\n" +
                "  Detail: Key (ip, port)=(127.0.0.1, 7778) already exists.)";

        DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(inputCause);
        ApiResponse response = ResponseMapping.exceptionConstrainMessage("New smpp server error test", dataIntegrityViolationException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertEquals("error", response.message());
        assertEquals(outputCommentError, response.comment());
    }

    @Test
    @DisplayName("exceptionConstrainMessage response when regex format does not match then do it successfully")
    void exceptionConstrainMessageWhenViolateUniqueConstrainMessageAndRegexFormatNotMatchTheDoItSuccessfully() {
        String inputCause = "could not execute statement [WARNING: duplicate key value violates unique constraint \"ukftxjkkockmiwbc4l4im7rpuok\"\n" +
                "  Detail: Key (ip, port)=(127.0.0.1, 7778) already exists.] [insert into smpp_server (created_at,created_by_id,enabled,ip,is_default,name,port,processor_degree,queue_capacity,status,transaction_timer,updated_at,updated_by_id,wait_for_bind,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)]; SQL [insert into smpp_server (created_at,created_by_id,enabled,ip,is_default,name,port,processor_degree,queue_capacity,status,transaction_timer,updated_at,updated_by_id,wait_for_bind,id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)]; constraint [ukftxjkkockmiwbc4l4im7rpuok]";

        DataIntegrityViolationException dataIntegrityViolationException = new DataIntegrityViolationException(inputCause);
        ApiResponse response = ResponseMapping.exceptionConstrainMessage("New smpp server error test", dataIntegrityViolationException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertEquals("error", response.message());
        assertTrue(response.comment().contains(inputCause)); // all message error is included
    }
}