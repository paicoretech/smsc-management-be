package com.smsc.management.app.ss7.controller;

import com.smsc.management.app.ss7.dto.SccpAddressesDTO;
import com.smsc.management.app.ss7.dto.SccpDTO;
import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.dto.SccpRemoteResourcesDTO;
import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.dto.SccpServiceAccessPointsDTO;
import com.smsc.management.app.ss7.mapper.SccpMapper;
import com.smsc.management.app.ss7.model.entity.Sccp;
import com.smsc.management.app.ss7.model.entity.SccpAddresses;
import com.smsc.management.app.ss7.model.entity.SccpMtp3Destinations;
import com.smsc.management.app.ss7.model.entity.SccpRemoteResources;
import com.smsc.management.app.ss7.model.entity.SccpRules;
import com.smsc.management.app.ss7.model.entity.SccpServiceAccessPoints;
import com.smsc.management.app.ss7.model.repository.SccpAddressesRepository;
import com.smsc.management.app.ss7.model.repository.SccpMtp3DestinationsRepository;
import com.smsc.management.app.ss7.model.repository.SccpRemoteResourcesRepository;
import com.smsc.management.app.ss7.model.repository.SccpRepository;
import com.smsc.management.app.ss7.model.repository.SccpRulesRepository;
import com.smsc.management.app.ss7.model.repository.SccpServiceAccessPointsRepository;
import com.smsc.management.app.ss7.utilsTest.Utils;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

class Ss7GatewaysControllerSccpTest extends BaseIntegrationTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Ss7GatewaysController ss7GatewaysController;

    @Autowired
    SccpMapper sccpMapper;

    @MockBean
    SccpRepository sccpRepository;

    @MockBean
    SccpRemoteResourcesRepository sccpRemoteResourcesRepository;

    @MockBean
    SccpServiceAccessPointsRepository sccpServiceAccessPointsRepository;

    @MockBean
    SccpMtp3DestinationsRepository sccpMtp3DestinationsRepository;

    @MockBean
    SccpAddressesRepository sccpAddressesRepository;

    @MockBean
    SccpRulesRepository sccpRulesRepository;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "sccp");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "sccp_remote_resources");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "sccp_service_access_points");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "sccp_mtp3_destinations");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "sccp_addresses");
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpTest() {
        SccpDTO sccpDTOMock = Utils.getSccpDTOMock();
        Sccp sccpEntityMock = sccpMapper.toEntity(sccpDTOMock);

        Mockito.when(sccpRepository.findByNetworkId(anyInt())).thenReturn(sccpEntityMock);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccp(5);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpRepository.findByExternalId(anyString())).thenReturn(sccpEntityMock);
        response = ss7GatewaysController.getSccpByExternalId("005");
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getSccp(5);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.getSccp(5);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpTest() {
        SccpDTO sccpDTOMock = Utils.newSccpDTOMock();

        SccpDTO savedSccpDTOMock = Utils.getSccpDTOMock();
        Sccp sccpEntityMock = sccpMapper.toEntity(savedSccpDTOMock);

        Mockito.when(sccpRepository.save(any())).thenReturn(sccpEntityMock);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccp(sccpDTOMock);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRepository).save(any());
        response = ss7GatewaysController.createSccp(sccpDTOMock);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpTest() {
        SccpDTO sccpDTOMockUpdated = Utils.getSccpDTOMock();
        sccpDTOMockUpdated.setZMarginXudtMessage(100);
        Sccp sccpEntityMockResponse = sccpMapper.toEntity(sccpDTOMockUpdated);

        SccpDTO sccpDTOMock = Utils.getSccpDTOMock();
        Sccp currentSccpEntityMock = sccpMapper.toEntity(sccpDTOMock);

        Mockito.when(sccpRepository.findById(anyInt())).thenReturn(currentSccpEntityMock);
        Mockito.when(sccpRepository.save(any())).thenReturn(sccpEntityMockResponse);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccp(sccpDTOMockUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var sccp = (SccpDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals(100, sccp.getZMarginXudtMessage());

        Mockito.when(sccpRepository.findByExternalId(anyString())).thenReturn(currentSccpEntityMock);
        response = ss7GatewaysController.updateSccpByExternalId(sccpDTOMockUpdated, "001");
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateSccp(sccpDTOMockUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRepository).findById(anyInt());
        response = ss7GatewaysController.updateSccp(sccpDTOMockUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpTest() {
        SccpDTO sccpDTOMock = Utils.getSccpDTOMock();
        Sccp currentSccpEntityMock = sccpMapper.toEntity(sccpDTOMock);

        Mockito.when(sccpRepository.findById(anyInt())).thenReturn(currentSccpEntityMock);
        Mockito.doNothing().when(sccpRepository).delete(currentSccpEntityMock);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccp(1);
        checkAssertions(response, HttpStatus.OK,"DELETE");

        Mockito.when(sccpRepository.findByExternalId(anyString())).thenReturn(currentSccpEntityMock);
        response = ss7GatewaysController.deleteSccpByExternalId("001");
        checkAssertions(response, HttpStatus.OK,"DELETE");

        Mockito.when(sccpRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccp(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRepository).findById(anyInt());
        response = ss7GatewaysController.deleteSccp(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpRemoteResourceTest() {
        SccpRemoteResourcesDTO sccpRemoteResourcesDTO = Utils.getSccpRemoteResourcesDTOMock();
        SccpRemoteResources sccpRemoteResources = sccpMapper.toEntityRemoteResources(sccpRemoteResourcesDTO);

        Mockito.when(sccpRemoteResourcesRepository.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpRemoteResources));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccpRemoteResource(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpRemoteResourcesRepository.findBySs7SccpId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getSccpRemoteResource(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRemoteResourcesRepository).findBySs7SccpId(anyInt());
        response = ss7GatewaysController.getSccpRemoteResource(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpRemoteResourceTest() {
        SccpRemoteResourcesDTO sccpRemoteResourcesDTO = Utils.newSccpRemoteResourcesDTOMock();

        SccpRemoteResourcesDTO savedSccpRemoteResourcesDTO  = Utils.getSccpRemoteResourcesDTOMock();
        SccpRemoteResources sccpRemoteResourcesEntityMock = sccpMapper.toEntityRemoteResources(savedSccpRemoteResourcesDTO);

        Mockito.when(sccpRemoteResourcesRepository.save(any())).thenReturn(sccpRemoteResourcesEntityMock);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccpRemoteResource(sccpRemoteResourcesDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRemoteResourcesRepository).save(any());
        response = ss7GatewaysController.createSccpRemoteResource(sccpRemoteResourcesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpRemoteResourceTest() {
        SccpRemoteResourcesDTO sccpRemoteResourcesDTOUpdated = Utils.getSccpRemoteResourcesDTOMock();
        sccpRemoteResourcesDTOUpdated.setMarkProhibited(false);
        SccpRemoteResources sccpRemoteResources = sccpMapper.toEntityRemoteResources(sccpRemoteResourcesDTOUpdated);

        SccpRemoteResourcesDTO sccpRemoteResourcesDTO = Utils.getSccpRemoteResourcesDTOMock();
        SccpRemoteResources currentSccpRemoteResources = sccpMapper.toEntityRemoteResources(sccpRemoteResourcesDTO);

        Mockito.when(sccpRemoteResourcesRepository.findById(anyInt())).thenReturn(currentSccpRemoteResources);
        Mockito.when(sccpRemoteResourcesRepository.save(any())).thenReturn(sccpRemoteResources);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccpRemoteResource(sccpRemoteResourcesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var sccp = (SccpRemoteResourcesDTO) Objects.requireNonNull(apiResponse).data();
        assertFalse(sccp.isMarkProhibited());

        Mockito.when(sccpRemoteResourcesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateSccpRemoteResource(sccpRemoteResourcesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRemoteResourcesRepository).findById(anyInt());
        response = ss7GatewaysController.updateSccpRemoteResource(sccpRemoteResourcesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpRemoteResourceTest() {
        SccpRemoteResourcesDTO sccpRemoteResourcesDTO = Utils.getSccpRemoteResourcesDTOMock();
        SccpRemoteResources currentSccpRemoteResources = sccpMapper.toEntityRemoteResources(sccpRemoteResourcesDTO);

        Mockito.when(sccpRemoteResourcesRepository.findById(anyInt())).thenReturn(currentSccpRemoteResources);
        Mockito.doNothing().when(sccpRemoteResourcesRepository).delete(currentSccpRemoteResources);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccpRemoteResource(1);
        checkAssertions(response, HttpStatus.OK,"DELETE");

        Mockito.when(sccpRemoteResourcesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccpRemoteResource(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRemoteResourcesRepository).findById(anyInt());
        response = ss7GatewaysController.deleteSccpRemoteResource(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpServiceAccessPointTest() {
        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTO = Utils.getSccpServiceAccessPointsDTOMock();
        SccpServiceAccessPoints sccpServiceAccessPoints = sccpMapper.toEntitySap(sccpServiceAccessPointsDTO);

        Mockito.when(sccpServiceAccessPointsRepository.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpServiceAccessPoints));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccpServiceAccessPoint(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpServiceAccessPointsRepository.findBySs7SccpId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getSccpServiceAccessPoint(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpServiceAccessPointsRepository).findBySs7SccpId(anyInt());
        response = ss7GatewaysController.getSccpServiceAccessPoint(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpServiceAccessPointTest() {
        SccpServiceAccessPointsDTO newSccpServiceAccessPointsDTO = Utils.newSccpServiceAccessPointsDTOMock();

        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTO = Utils.getSccpServiceAccessPointsDTOMock();
        SccpServiceAccessPoints sccpServiceAccessPoints = sccpMapper.toEntitySap(sccpServiceAccessPointsDTO);

        Mockito.when(sccpServiceAccessPointsRepository.save(any())).thenReturn(sccpServiceAccessPoints);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccpServiceAccessPoint(newSccpServiceAccessPointsDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpServiceAccessPointsRepository).save(any());
        response = ss7GatewaysController.createSccpServiceAccessPoint(newSccpServiceAccessPointsDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpServiceAccessPointTest() {
        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTOUpdated = Utils.getSccpServiceAccessPointsDTOMock();
        sccpServiceAccessPointsDTOUpdated.setName("updating name");
        SccpServiceAccessPoints sccpServiceAccessPoints = sccpMapper.toEntitySap(sccpServiceAccessPointsDTOUpdated);

        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTO = Utils.getSccpServiceAccessPointsDTOMock();
        SccpServiceAccessPoints currentSccpServiceAccessPoints = sccpMapper.toEntitySap(sccpServiceAccessPointsDTO);

        Mockito.when(sccpServiceAccessPointsRepository.findById(anyInt())).thenReturn(currentSccpServiceAccessPoints);
        Mockito.when(sccpServiceAccessPointsRepository.save(any())).thenReturn(sccpServiceAccessPoints);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccpServiceAccessPoint(sccpServiceAccessPointsDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var sccp = (SccpServiceAccessPointsDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals("updating name", sccp.getName());

        Mockito.when(sccpServiceAccessPointsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateSccpServiceAccessPoint(sccpServiceAccessPointsDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpServiceAccessPointsRepository.findById(anyInt())).thenReturn(currentSccpServiceAccessPoints);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpServiceAccessPointsRepository).save(any());
        response = ss7GatewaysController.updateSccpServiceAccessPoint(sccpServiceAccessPointsDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpServiceAccessPointTest() {
        SccpServiceAccessPointsDTO sccpServiceAccessPointsDTO = Utils.getSccpServiceAccessPointsDTOMock();
        SccpServiceAccessPoints currentSccpServiceAccessPoints = sccpMapper.toEntitySap(sccpServiceAccessPointsDTO);

        Mockito.when(sccpServiceAccessPointsRepository.findById(anyInt())).thenReturn(currentSccpServiceAccessPoints);
        Mockito.doNothing().when(sccpServiceAccessPointsRepository).delete(currentSccpServiceAccessPoints);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccpServiceAccessPoint(1);
        checkAssertions(response, HttpStatus.OK,"DELETE");

        Mockito.when(sccpServiceAccessPointsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccpServiceAccessPoint(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpServiceAccessPointsRepository).findById(anyInt());
        response = ss7GatewaysController.deleteSccpServiceAccessPoint(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpMtp3DestinationsTest() {
        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTO = Utils.getSccpMtp3DestinationsDTOMock();

        Mockito.when(sccpMtp3DestinationsRepository.fetchMtp3Destinations(anyInt())).thenReturn(List.of(sccpMtp3DestinationsDTO));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccpMtp3Destinations(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpMtp3DestinationsRepository.fetchMtp3Destinations(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getSccpMtp3Destinations(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpMtp3DestinationsRepository).fetchMtp3Destinations(anyInt());
        response = ss7GatewaysController.getSccpMtp3Destinations(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpMtp3DestinationsTest() {
        SccpMtp3DestinationsDTO newSccpMtp3DestinationsDTO = Utils.newSccpMtp3DestinationsDTOMock();

        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTO = Utils.getSccpMtp3DestinationsDTOMock();
        SccpMtp3Destinations savedSccpMtp3Destinations = sccpMapper.toEntityMTP3(sccpMtp3DestinationsDTO);

        Mockito.when(sccpMtp3DestinationsRepository.save(any())).thenReturn(savedSccpMtp3Destinations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccpMtp3Destinations(newSccpMtp3DestinationsDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpMtp3DestinationsRepository).save(any());
        response = ss7GatewaysController.createSccpMtp3Destinations(newSccpMtp3DestinationsDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpMtp3DestinationsTest() {
        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTOUpdated = Utils.getSccpMtp3DestinationsDTOMock();
        sccpMtp3DestinationsDTOUpdated.setName("updating name");
        SccpMtp3Destinations sccpMtp3Destinations = sccpMapper.toEntityMTP3(sccpMtp3DestinationsDTOUpdated);

        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTO = Utils.getSccpMtp3DestinationsDTOMock();
        SccpMtp3Destinations currentSccpMtp3Destinations = sccpMapper.toEntityMTP3(sccpMtp3DestinationsDTO);

        Mockito.when(sccpMtp3DestinationsRepository.findById(anyInt())).thenReturn(currentSccpMtp3Destinations);
        Mockito.when(sccpMtp3DestinationsRepository.save(any())).thenReturn(sccpMtp3Destinations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccpMtp3Destinations(sccpMtp3DestinationsDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var sccp = (SccpMtp3DestinationsDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals("updating name", sccp.getName());

        Mockito.when(sccpMtp3DestinationsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateSccpMtp3Destinations(sccpMtp3DestinationsDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpMtp3DestinationsRepository.findById(anyInt())).thenReturn(currentSccpMtp3Destinations);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpMtp3DestinationsRepository).save(any());
        response = ss7GatewaysController.updateSccpMtp3Destinations(sccpMtp3DestinationsDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpMtp3DestinationsTest() {
        SccpMtp3DestinationsDTO sccpMtp3DestinationsDTO = Utils.getSccpMtp3DestinationsDTOMock();
        SccpMtp3Destinations currentSccpMtp3Destinations = sccpMapper.toEntityMTP3(sccpMtp3DestinationsDTO);

        Mockito.when(sccpMtp3DestinationsRepository.findById(anyInt())).thenReturn(currentSccpMtp3Destinations);
        Mockito.doNothing().when(sccpMtp3DestinationsRepository).delete(currentSccpMtp3Destinations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccpMtp3Destinations(1);
        checkAssertions(response, HttpStatus.OK,"DELETE");

        Mockito.when(sccpMtp3DestinationsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccpMtp3Destinations(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpMtp3DestinationsRepository.findById(anyInt())).thenReturn(currentSccpMtp3Destinations);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpMtp3DestinationsRepository).delete(any());
        response = ss7GatewaysController.deleteSccpMtp3Destinations(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpAddressTest() {
        SccpAddressesDTO sccpAddressesDTO = Utils.getSccpAddressesDTOMock();
        SccpAddresses sccpAddresses = sccpMapper.toEntityAddress(sccpAddressesDTO);

        Mockito.when(sccpAddressesRepository.findBySs7SccpId(anyInt())).thenReturn(List.of(sccpAddresses));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccpAddress(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpAddressesRepository.findBySs7SccpId(anyInt())).thenReturn(Collections.emptyList());
        response = ss7GatewaysController.getSccpAddress(1);
        checkAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpAddressTest() {
        SccpAddressesDTO newSccpAddressesDTO = Utils.newSccpAddressesDTOMock();

        SccpAddressesDTO sccpAddressesDTO = Utils.getSccpAddressesDTOMock();
        SccpAddresses sccpAddresses = sccpMapper.toEntityAddress(sccpAddressesDTO);

        Mockito.when(sccpAddressesRepository.save(any())).thenReturn(sccpAddresses);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.OK);

        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.OK);

        // gtIndicator = "0001"
        newSccpAddressesDTO.setAddressIndicator(4);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        // gtIndicator = "0010"
        newSccpAddressesDTO.setAddressIndicator(8);
        newSccpAddressesDTO.setTranslationType(-1);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newSccpAddressesDTO.setAddressIndicator(8);
        newSccpAddressesDTO.setTranslationType(256);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        // gtIndicator = "0011"
        newSccpAddressesDTO.setAddressIndicator(12);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newSccpAddressesDTO.setTranslationType(10);
        newSccpAddressesDTO.setNumberingPlanId(-1);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        // gtIndicator = "0100"
        newSccpAddressesDTO.setAddressIndicator(16);
        newSccpAddressesDTO.setTranslationType(-1);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newSccpAddressesDTO.setTranslationType(10);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newSccpAddressesDTO.setNatureOfAddressId(-1);
        newSccpAddressesDTO.setTranslationType(10);
        newSccpAddressesDTO.setNumberingPlanId(1);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        // validate valid value to address indicator
        newSccpAddressesDTO.setAddressIndicator(20);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newSccpAddressesDTO.setAddressIndicator(1);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newSccpAddressesDTO.setAddressIndicator(2);
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpAddressesRepository).save(any());
        response = ss7GatewaysController.createSccpAddress(newSccpAddressesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpAddressTest() {
        SccpAddressesDTO sccpAddressesDTOUpdated = Utils.getSccpAddressesDTOMock();
        sccpAddressesDTOUpdated.setName("updating name");
        SccpAddresses savedSccpAddresses = sccpMapper.toEntityAddress(sccpAddressesDTOUpdated);

        SccpAddressesDTO sccpAddressesDTO = Utils.getSccpAddressesDTOMock();
        SccpAddresses currentSccpAddresses = sccpMapper.toEntityAddress(sccpAddressesDTO);

        Mockito.when(sccpAddressesRepository.findById(anyInt())).thenReturn(currentSccpAddresses);
        Mockito.when(sccpAddressesRepository.save(any())).thenReturn(savedSccpAddresses);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccpAddress(sccpAddressesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var sccp = (SccpAddressesDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals("updating name", sccp.getName());

        Mockito.when(sccpAddressesRepository.findById(anyInt())).thenReturn(null);
        Mockito.when(sccpAddressesRepository.save(any())).thenReturn(savedSccpAddresses);
        response = ss7GatewaysController.updateSccpAddress(sccpAddressesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpAddressesRepository.findById(anyInt())).thenReturn(currentSccpAddresses);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpAddressesRepository).save(any());
        response = ss7GatewaysController.updateSccpAddress(sccpAddressesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpAddressTest() {
        SccpAddressesDTO sccpAddressesDTO = Utils.getSccpAddressesDTOMock();
        SccpAddresses currentSccpAddresses = sccpMapper.toEntityAddress(sccpAddressesDTO);

        Mockito.when(sccpAddressesRepository.findById(anyInt())).thenReturn(currentSccpAddresses);
        Mockito.doNothing().when(sccpAddressesRepository).delete(any());
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccpAddress(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.when(sccpAddressesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccpAddress(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpAddressesRepository.findById(anyInt())).thenReturn(currentSccpAddresses);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpAddressesRepository).delete(any());
        response = ss7GatewaysController.deleteSccpAddress(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpRulesTest() {
        SccpRulesDTO sccpRulesDTO = Utils.getSccpRulesDTOMock();

        Mockito.when(sccpRulesRepository.fetchSccpRules(anyInt())).thenReturn(List.of(sccpRulesDTO));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccpRules(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpRulesRepository.fetchSccpRules(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getSccpRules(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRulesRepository).fetchSccpRules(anyInt());
        response = ss7GatewaysController.getSccpRules(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpRulesTest() {
        SccpRulesDTO newSccpRulesDTO = Utils.newSccpRulesDTOMock();

        SccpRulesDTO sccpRulesDTO = Utils.getSccpRulesDTOMock();
        SccpRules savedSccpRules = sccpMapper.toEntityRules(sccpRulesDTO);

        Mockito.when(sccpRulesRepository.save(any())).thenReturn(savedSccpRules);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccpRules(newSccpRulesDTO);
        checkAssertions(response, HttpStatus.OK);

        newSccpRulesDTO.setCallingAddressIndicator(null);
        response = ss7GatewaysController.createSccpRules(newSccpRulesDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRulesRepository).save(any());
        response = ss7GatewaysController.createSccpRules(newSccpRulesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpRulesTest() {
        SccpRulesDTO sccpRulesDTOUpdated = Utils.getSccpRulesDTOMock();
        sccpRulesDTOUpdated.setName("updating name");
        SccpRules savedSccpRules = sccpMapper.toEntityRules(sccpRulesDTOUpdated);

        SccpRulesDTO sccpRulesDTO = Utils.getSccpRulesDTOMock();
        SccpRules currentSavedSccpRules = sccpMapper.toEntityRules(sccpRulesDTO);

        Mockito.when(sccpRulesRepository.findById(anyInt())).thenReturn(currentSavedSccpRules);
        Mockito.when(sccpRulesRepository.save(any())).thenReturn(savedSccpRules);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccpRules(sccpRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var sccp = (SccpRulesDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals("updating name", sccp.getName());

        sccpRulesDTOUpdated.setCallingAddressIndicator(null);
        response = ss7GatewaysController.updateSccpRules(sccpRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        apiResponse = response.getBody();
        sccp = (SccpRulesDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals("updating name", sccp.getName());

        Mockito.when(sccpRulesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateSccpRules(sccpRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        sccpRulesDTOUpdated.setPrimaryAddressId(null);
        sccpRulesDTOUpdated.setSecondaryAddressId(null);
        response = ss7GatewaysController.updateSccpRules(sccpRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpRulesTest() {
        SccpRulesDTO sccpRulesDTO = Utils.getSccpRulesDTOMock();
        SccpRules currentSavedSccpRules = sccpMapper.toEntityRules(sccpRulesDTO);

        Mockito.when(sccpRulesRepository.findById(anyInt())).thenReturn(currentSavedSccpRules);
        Mockito.doNothing().when(sccpRulesRepository).delete(any());
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccpRules(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.when(sccpRulesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccpRules(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpRulesRepository).findById(anyInt());
        response = ss7GatewaysController.deleteSccpRules(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
