package com.smsc.management.app.settings.service;

import com.smsc.management.app.settings.dto.GeneralSettingsSmppHttpDTO;
import com.smsc.management.app.settings.dto.GeneralSmscRetryDTO;
import com.smsc.management.app.settings.mapper.GeneralSettingsMapper;
import com.smsc.management.app.settings.model.repository.GeneralSettingsSmppHttpRepository;
import com.smsc.management.app.settings.model.repository.GeneralSmscRetryRepository;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GeneralSettingsServiceTest {

    @Mock
    private GeneralSettingsSmppHttpRepository generalSettingsHttpSmppRepo;

    @Mock
    private GeneralSmscRetryRepository generalSmscRetryRepo;

    @Mock
    private GeneralSettingsMapper generalSettingsMapper;

    @Mock
    private AppProperties appProperties;

    @Mock
    private UtilsBase utilsBase;

    @InjectMocks
    private GeneralSettingsService generalSettingsService;


    @Test
    void getGeneralSettingsSmppHttpTestNotFound() {
        Mockito.when(this.generalSettingsHttpSmppRepo.findById(1)).thenReturn(null);
        var response = generalSettingsService.getGeneralSettingsSmppHttp();
        assertNotNull(response);
        assertEquals(404, response.status());
        assertEquals("General settings to smpp/http was not found", response.comment());
    }

    @Test
    void getGeneralSettingsSmppHttpTestError() {
        Mockito.when(this.generalSettingsHttpSmppRepo.findById(1)).thenThrow(NullPointerException.class);
        var response = generalSettingsService.getGeneralSettingsSmppHttp();
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("Error to get general settings smpp-http (null)", response.comment());
    }

    @Test
    void updateGeneralSettingsTestNotFound() {
        Mockito.when(this.generalSettingsHttpSmppRepo.findById(1)).thenReturn(null);
        var response = generalSettingsService.updateGeneralSettings(new GeneralSettingsSmppHttpDTO());
        assertNotNull(response);
        assertEquals(404, response.status());
        assertEquals("General settings to smpp/http was not found", response.comment());
    }

    @Test
    void updateGeneralSettingsTestNotError() {
        Mockito.when(this.generalSettingsHttpSmppRepo.findById(1)).thenThrow(NullPointerException.class);
        var response = generalSettingsService.updateGeneralSettings(new GeneralSettingsSmppHttpDTO());
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("Error to update General Settings smpp-http (null)", response.comment());
    }

    @Test
    void getGeneralSmscRetryTestNotFound() {
        Mockito.when(this.generalSmscRetryRepo.findById(1)).thenReturn(null);
        var response = generalSettingsService.getGeneralSmscRetry();
        assertNotNull(response);
        assertEquals(404, response.status());
        assertEquals("General smsc retry was not found.", response.comment());
    }

    @Test
    void getGeneralSmscRetryTestError() {
        Mockito.when(this.generalSmscRetryRepo.findById(1)).thenThrow(NullPointerException.class);
        var response = generalSettingsService.getGeneralSmscRetry();
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("Error to get general smsc retry (null)", response.comment());
    }

    @Test
    void updateGeneralSmscRetryTestNotFound() {
        Mockito.when(this.generalSmscRetryRepo.findById(1)).thenReturn(null);
        var response = generalSettingsService.updateGeneralSmscRetry(new GeneralSmscRetryDTO());
        assertNotNull(response);
        assertEquals(404, response.status());
        assertEquals("General smsc retry was not found", response.comment());
    }

    @Test
    void updateGeneralSmscRetryTestNotError() {
        Mockito.when(this.generalSmscRetryRepo.findById(1)).thenThrow(NullPointerException.class);
        var response = generalSettingsService.updateGeneralSmscRetry(new GeneralSmscRetryDTO());
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("Error to update general smsc retry (null)", response.comment());
    }

}