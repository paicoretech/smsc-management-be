package com.smsc.management.app.ss7.controller;

import com.paicbd.smsc.scylla.ScyllaManager;
import com.smsc.management.app.ss7.dto.HomeRoutingCcMccMncDTO;
import com.smsc.management.app.ss7.dto.HomeRoutingDTO;
import com.smsc.management.app.ss7.mapper.HomeRoutingMapper;
import com.smsc.management.app.ss7.model.entity.HomeRouting;
import com.smsc.management.app.ss7.model.entity.HomeRoutingCcMccMnc;
import com.smsc.management.app.ss7.model.repository.HomeRoutingCcMccMncRepository;
import com.smsc.management.app.ss7.model.repository.HomeRoutingRepository;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
import com.smsc.management.app.ss7.utilsTest.Utils;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
class Ss7GatewaysControllerHrTest extends BaseIntegrationTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ScyllaManager scyllaManager;

    @Autowired
    Ss7GatewaysController ss7GatewaysController;

    @Autowired
    HomeRoutingMapper homeRoutingMapper;

    @MockBean
    Ss7GatewaysRepository ss7GatewaysRepository;

    @MockBean
    HomeRoutingRepository homeRoutingRepository;

    @MockBean
    HomeRoutingCcMccMncRepository homeRoutingCcMncRepository;

    @MockBean
    ObjectSs7Service objectSs7Service;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "home_routing_cc_mcc_mnc");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "home_routing");
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getHomeRoutingTest() {
        HomeRoutingDTO homeRoutingDTO = Utils.getHomeRoutingDTOMock();
        HomeRouting homeRouting = homeRoutingMapper.toEntity(homeRoutingDTO);

        when(homeRoutingRepository.findByNetworkId(anyInt())).thenReturn(homeRouting);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getHomeRouting(3);
        checkAssertions(response, HttpStatus.OK);

        when(homeRoutingRepository.findByExternalId(anyString())).thenReturn(homeRouting);
        response = ss7GatewaysController.getHomeRoutingByExternalId("003");
        checkAssertions(response, HttpStatus.OK);

        when(homeRoutingRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getHomeRouting(3);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(homeRoutingRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.getHomeRouting(3);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createHomeRoutingTest() {
        HomeRoutingDTO homeRoutingDTOResponse = Utils.getHomeRoutingDTOMock();
        HomeRouting homeRoutingEntitySaved = homeRoutingMapper.toEntity(homeRoutingDTOResponse);
        HomeRoutingDTO homeRoutingDTO = Utils.newHomeRoutingDTOMock();

        when(homeRoutingRepository.save(any())).thenReturn(homeRoutingEntitySaved);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createHomeRouting(homeRoutingDTO);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (HomeRoutingDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals(1, apiResponse.getId());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(homeRoutingRepository).save(any());
        response = ss7GatewaysController.createHomeRouting(homeRoutingDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateHomeRoutingTest() {
        HomeRoutingDTO homeRoutingDTO = Utils.getHomeRoutingDTOMock();
        HomeRouting currentHomeRoutingEntity = homeRoutingMapper.toEntity(homeRoutingDTO);

        HomeRoutingDTO updatedHomeRoutingDTO= Utils.getHomeRoutingDTOMock();
        updatedHomeRoutingDTO.setNetworkId(5);
        updatedHomeRoutingDTO.setExternalId("005");
        HomeRouting updatedHomeRoutingEntity= homeRoutingMapper.toEntity(updatedHomeRoutingDTO);

        when(homeRoutingRepository.findById(anyInt())).thenReturn(currentHomeRoutingEntity);
        when(homeRoutingRepository.save(any())).thenReturn(updatedHomeRoutingEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateHomeRouting(homeRoutingDTO, 1);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (HomeRoutingDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals(5, apiResponse.getNetworkId());

        when(homeRoutingRepository.findByExternalId(anyString())).thenReturn(currentHomeRoutingEntity);
        response = ss7GatewaysController.updateHomeRoutingByExternalId(homeRoutingDTO, "005");
        checkAssertions(response, HttpStatus.OK);

        when(homeRoutingRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateHomeRouting(homeRoutingDTO, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        when(homeRoutingRepository.findById(anyInt())).thenReturn(currentHomeRoutingEntity);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(homeRoutingRepository).save(any());
        response = ss7GatewaysController.updateHomeRouting(homeRoutingDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteHomeRoutingTest() {
        HomeRoutingDTO homeRoutingDTO = Utils.getHomeRoutingDTOMock();
        HomeRouting currentHomeRoutingEntity = homeRoutingMapper.toEntity(homeRoutingDTO);

        HomeRoutingCcMccMncDTO childDto = Utils.getHomeRoutingCcMccMncDTOMock();
        HomeRoutingCcMccMnc child = homeRoutingMapper.toEntityCcMccMnc(childDto);
        child.setSs7HomeRoutingId((int) currentHomeRoutingEntity.getId());
        List<HomeRoutingCcMccMnc> children = new ArrayList<>();
        children.add(child);

        when(homeRoutingRepository.findByNetworkId(anyInt())).thenReturn(currentHomeRoutingEntity);
        when(homeRoutingRepository.findById(anyInt())).thenReturn(currentHomeRoutingEntity);
        when(homeRoutingCcMncRepository.findBySs7HomeRoutingId(anyInt())).thenReturn(children);
        doNothing().when(scyllaManager).deleteHomeRoutingByNetworkCountryAndMcc(anyString(), anyString(), anyString());
        doNothing().when(homeRoutingCcMncRepository).deleteAll(children);
        doNothing().when(homeRoutingRepository).delete(currentHomeRoutingEntity);

        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteHomeRouting(3);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.verify(homeRoutingCcMncRepository).findBySs7HomeRoutingId((int) currentHomeRoutingEntity.getId());
        Mockito.verify(scyllaManager).deleteHomeRoutingByNetworkCountryAndMcc(
                String.valueOf(currentHomeRoutingEntity.getNetworkId()),
                child.getCountryCode(),
                child.getMccMnc()
        );
        Mockito.verify(homeRoutingCcMncRepository).deleteAll(children);
        Mockito.verify(homeRoutingRepository).delete(currentHomeRoutingEntity);

        when(homeRoutingRepository.findByExternalId(anyString())).thenReturn(currentHomeRoutingEntity);
        response = ss7GatewaysController.deleteHomeRoutingByExternalId("005");
        checkAssertions(response, HttpStatus.OK, "DELETE");

        when(homeRoutingRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteHomeRouting(3);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        when(homeRoutingRepository.findByNetworkId(anyInt())).thenReturn(currentHomeRoutingEntity);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(homeRoutingRepository).delete(currentHomeRoutingEntity);
        response = ss7GatewaysController.deleteHomeRouting(3);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getHomeRoutingCcMccMncTest() {
        HomeRoutingCcMccMncDTO homeRoutingCcMccMncDTO = Utils.getHomeRoutingCcMccMncDTOMock();
        List<HomeRoutingCcMccMnc> homeRoutingCcMccMncList = new ArrayList<>();
        homeRoutingCcMccMncList.add(homeRoutingMapper.toEntityCcMccMnc(homeRoutingCcMccMncDTO));

        when(homeRoutingCcMncRepository.findBySs7HomeRoutingId(anyInt())).thenReturn(homeRoutingCcMccMncList);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getCcMccMnc(1);
        checkAssertions(response, HttpStatus.OK);

        when(homeRoutingCcMncRepository.findBySs7HomeRoutingId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getCcMccMnc(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(homeRoutingCcMncRepository).findBySs7HomeRoutingId(anyInt());
        response = ss7GatewaysController.getCcMccMnc(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createRoutingCcMccMncTest() {
        HomeRoutingDTO hrDto = Utils.getHomeRoutingDTOMock();
        HomeRouting parent = homeRoutingMapper.toEntity(hrDto);

        HomeRoutingCcMccMncDTO newHomeRoutingCcMccMncDTO = Utils.newHomeRoutingCcMccMncDTOMock();
        newHomeRoutingCcMccMncDTO.setSs7HomeRoutingId((int) parent.getId());

        HomeRoutingCcMccMnc savedEntity = homeRoutingMapper.toEntityCcMccMnc(newHomeRoutingCcMccMncDTO);
        savedEntity.setId(123L);

        when(homeRoutingRepository.findById(anyInt())).thenReturn(parent);
        when(homeRoutingCcMncRepository.save(any())).thenReturn(savedEntity);
        doNothing().when(scyllaManager).insertHomeRouting(anyString(), anyString(), anyString(), anyString());

        ResponseEntity<ApiResponse> response = ss7GatewaysController.createCcMccMnc(newHomeRoutingCcMccMncDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.verify(scyllaManager).insertHomeRouting(
                String.valueOf(parent.getNetworkId()),
                newHomeRoutingCcMccMncDTO.getCountryCode(),
                newHomeRoutingCcMccMncDTO.getMccMnc(),
                newHomeRoutingCcMccMncDTO.getSmsc()
        );

        Mockito.doThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"))
                .when(homeRoutingCcMncRepository).save(any());

        response = ss7GatewaysController.createCcMccMnc(newHomeRoutingCcMccMncDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(homeRoutingCcMncRepository).save(any());
        response = ss7GatewaysController.createCcMccMnc(newHomeRoutingCcMccMncDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.verify(scyllaManager, Mockito.times(1))
                .insertHomeRouting(anyString(), anyString(), anyString(), anyString());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateCcMccMncController() throws Exception {
        var hrDto = Utils.getHomeRoutingDTOMock();
        var parent = homeRoutingMapper.toEntity(hrDto);

        var currentDto = Utils.getHomeRoutingCcMccMncDTOMock();
        currentDto.setCountryCode("505");
        currentDto.setMccMnc("71002");
        var currentEntity = homeRoutingMapper.toEntityCcMccMnc(currentDto);
        currentEntity.setId(200L);
        currentEntity.setSs7HomeRoutingId((int) parent.getId());

        var updatedDto = Utils.getHomeRoutingCcMccMncDTOMock();
        updatedDto.setCountryCode("506");
        updatedDto.setMccMnc("71099");
        updatedDto.setSmsc("123");

        var updatedEntity = homeRoutingMapper.toEntityCcMccMnc(updatedDto);
        updatedEntity.setId(currentEntity.getId());
        updatedEntity.setSs7HomeRoutingId(currentEntity.getSs7HomeRoutingId());

        when(homeRoutingRepository.findById(anyInt())).thenReturn(parent);
        when(homeRoutingCcMncRepository.findById(anyLong())).thenReturn(currentEntity);
        when(homeRoutingCcMncRepository.save(any())).thenReturn(updatedEntity);

        doNothing().when(scyllaManager).deleteHomeRoutingByNetworkCountryAndMcc(anyString(), anyString(), anyString());
        doNothing().when(scyllaManager).insertHomeRouting(anyString(), anyString(), anyString(), anyString());
        clearInvocations(scyllaManager);

        var response = ss7GatewaysController.updateCcMccMnc(updatedDto, 1);

        checkAssertions(response, HttpStatus.OK);

        var api = Objects.requireNonNull(response.getBody());
        var body = (HomeRoutingCcMccMncDTO) api.data();
        assertNotNull(body);
        assertEquals("123", body.getSmsc());
        assertEquals("71099", body.getMccMnc());

        var networkIdStr = String.valueOf(parent.getNetworkId());
        var oldCountry = "505";
        var newCountry = "506";

        verify(scyllaManager, times(1)).deleteHomeRoutingByNetworkCountryAndMcc(
                networkIdStr, oldCountry, "71002"
        );
        verify(scyllaManager, times(1)).insertHomeRouting(
                networkIdStr, newCountry, "71099", "123"
        );

        when(homeRoutingRepository.findById(anyInt())).thenReturn(parent);
        when(homeRoutingCcMncRepository.findById(anyLong())).thenReturn(currentEntity);

        Mockito.doThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"))
                .when(homeRoutingCcMncRepository).save(any());

        response = ss7GatewaysController.updateCcMccMnc(updatedDto, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        verify(scyllaManager, times(1)).deleteHomeRoutingByNetworkCountryAndMcc(
                anyString(), anyString(), anyString()
        );
        verify(scyllaManager, times(1)).insertHomeRouting(
                anyString(), anyString(), anyString(), anyString()
        );

        Mockito.doThrow(new RuntimeException("Test Exception"))
                .when(homeRoutingCcMncRepository).save(any());

        response = ss7GatewaysController.updateCcMccMnc(updatedDto, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        verify(scyllaManager, times(1)).deleteHomeRoutingByNetworkCountryAndMcc(
                anyString(), anyString(), anyString()
        );
        verify(scyllaManager, times(1)).insertHomeRouting(
                anyString(), anyString(), anyString(), anyString()
        );

        when(homeRoutingRepository.findById(anyInt())).thenReturn(parent);
        when(homeRoutingCcMncRepository.findById(anyLong())).thenReturn(null);

        response = ss7GatewaysController.updateCcMccMnc(updatedDto, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        verify(scyllaManager, times(1)).deleteHomeRoutingByNetworkCountryAndMcc(
                anyString(), anyString(), anyString()
        );
        verify(scyllaManager, times(1)).insertHomeRouting(
                anyString(), anyString(), anyString(), anyString()
        );
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteCcMccMncControllerTest() throws Exception {
        HomeRoutingCcMccMncDTO dto = Utils.getHomeRoutingCcMccMncDTOMock();
        HomeRoutingCcMccMnc entity = homeRoutingMapper.toEntityCcMccMnc(dto);

        when(homeRoutingCcMncRepository.findById(anyLong())).thenReturn(entity);
        doNothing().when(homeRoutingCcMncRepository).delete(entity);
        doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteCcMccMnc(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        when(homeRoutingCcMncRepository.findById(anyLong())).thenReturn(null);
        response = ss7GatewaysController.deleteCcMccMnc(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        when(homeRoutingCcMncRepository.findById(anyLong())).thenReturn(entity);
        Mockito.doThrow(new RuntimeException("Test Exception"))
                .when(homeRoutingCcMncRepository).delete(entity);
        response = ss7GatewaysController.deleteCcMccMnc(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.doThrow(new RuntimeException("Test Exception"))
                .when(homeRoutingCcMncRepository).findById(anyLong());
        response = ss7GatewaysController.deleteCcMccMnc(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}