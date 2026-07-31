package com.smsc.management.app.ss7.controller;

import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaDTO;
import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.dto.M3uaSocketsDTO;
import com.smsc.management.app.ss7.mapper.M3uaMapper;
import com.smsc.management.app.ss7.model.entity.M3ua;
import com.smsc.management.app.ss7.model.entity.M3uaAppServersRoutes;
import com.smsc.management.app.ss7.model.entity.M3uaApplicationServer;
import com.smsc.management.app.ss7.model.entity.M3uaAssAppServers;
import com.smsc.management.app.ss7.model.entity.M3uaAssociations;
import com.smsc.management.app.ss7.model.entity.M3uaRoutes;
import com.smsc.management.app.ss7.model.entity.M3uaSockets;
import com.smsc.management.app.ss7.model.repository.M3uaAppServersRouteRepository;
import com.smsc.management.app.ss7.model.repository.M3uaApplicationServerRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAssAppServersRepository;
import com.smsc.management.app.ss7.model.repository.M3uaAssociationsRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRepository;
import com.smsc.management.app.ss7.model.repository.M3uaRoutesRepository;
import com.smsc.management.app.ss7.model.repository.M3uaSocketsRepository;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

class Ss7GatewaysControllerM3uaTest extends BaseIntegrationTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Ss7GatewaysController ss7GatewaysController;

    @Autowired
    M3uaMapper m3uaMapper;

    @MockBean
    M3uaRepository m3uaRepository;

    @MockBean
    M3uaSocketsRepository m3uaSocketsRepository;

    @MockBean
    Ss7GatewaysRepository ss7GatewaysRepository;

    @MockBean
    M3uaAssociationsRepository m3uaAssociationsRepository;

    @MockBean
    M3uaApplicationServerRepository m3uaApplicationServerRepository;

    @MockBean
    M3uaAssAppServersRepository m3uaAssAppServersRepository;

    @MockBean
    M3uaRoutesRepository m3uaRoutesRepository;

    @MockBean
    M3uaAppServersRouteRepository m3uaAppServersRouteRepository;

    @MockBean
    ObjectSs7Service objectSs7Service;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "m3ua");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "m3ua_sockets");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "m3ua_associations");
        JdbcTestUtils.deleteFromTables(jdbcTemplate,"m3ua_application_server");
        JdbcTestUtils.deleteFromTables(jdbcTemplate,"m3ua_routes");
        JdbcTestUtils.deleteFromTables(jdbcTemplate,"m3ua_ass_app_servers");
        JdbcTestUtils.deleteFromTables(jdbcTemplate,"m3ua_app_servers_routes");
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getM3uaTest() {
        M3uaDTO m3uaDTO = Utils.getM3uatDtoMock();
        M3ua m3uaEntity = m3uaMapper.toEntity(m3uaDTO);

        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(m3uaEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getM3ua(3);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(m3uaRepository.findByExternalId(anyString())).thenReturn(m3uaEntity);
        response = ss7GatewaysController.getM3uaByExternalId("003");
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getM3ua(3);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.getM3ua(3);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createM3uaTest() {
        M3uaDTO m3uaDTOResponse = Utils.getM3uatDtoMock();
        M3ua m3uaEntitySaved = m3uaMapper.toEntity(m3uaDTOResponse);
        M3uaDTO m3uaDTO = Utils.newM3uatDtoMock();

        Mockito.when(m3uaRepository.save(any())).thenReturn(m3uaEntitySaved);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createM3ua(m3uaDTO);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (M3uaDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals(1, apiResponse.getId());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRepository).save(any());
        response = ss7GatewaysController.createM3ua(m3uaDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateM3uaTest() {
        M3uaDTO m3uaDTO = Utils.getM3uatDtoMock();
        M3ua currentM3uaEntity = m3uaMapper.toEntity(m3uaDTO);

        M3uaDTO updatedM3uaDTO = Utils.getM3uatDtoMock();
        updatedM3uaDTO.setNetworkId(5);
        updatedM3uaDTO.setExternalId("005");
        M3ua updatedM3uaEntity= m3uaMapper.toEntity(updatedM3uaDTO);

        Mockito.when(m3uaRepository.findById(anyInt())).thenReturn(currentM3uaEntity);
        Mockito.when(m3uaRepository.save(any())).thenReturn(updatedM3uaEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateM3ua(m3uaDTO, 1);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (M3uaDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals(5, apiResponse.getNetworkId());

        Mockito.when(m3uaRepository.findByExternalId(anyString())).thenReturn(currentM3uaEntity);
        response = ss7GatewaysController.updateM3uaByExternalId(m3uaDTO, "005");
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(m3uaRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateM3ua(m3uaDTO, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(m3uaRepository.findById(anyInt())).thenReturn(currentM3uaEntity);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRepository).save(any());
        response = ss7GatewaysController.updateM3ua(m3uaDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteM3uaTest() {
        M3uaDTO m3uaDTO = Utils.getM3uatDtoMock();
        M3ua currentM3uaEntity = m3uaMapper.toEntity(m3uaDTO);

        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(currentM3uaEntity);
        Mockito.doNothing().when(m3uaRepository).delete(currentM3uaEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteM3ua(3);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.when(m3uaRepository.findByExternalId(anyString())).thenReturn(currentM3uaEntity);
        Mockito.doNothing().when(m3uaRepository).delete(currentM3uaEntity);
        response = ss7GatewaysController.deleteM3uaByExternalId("003");
        checkAssertions(response, HttpStatus.OK, "DELETE");


        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteM3ua(3);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(m3uaRepository.findByNetworkId(anyInt())).thenReturn(currentM3uaEntity);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRepository).delete(currentM3uaEntity);
        response = ss7GatewaysController.deleteM3ua(3);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getM3uaSocketsTest() {
        M3uaSocketsDTO m3uaSocketsDTO = Utils.getM3uaSocketsMock();
        List<M3uaSockets> m3uaServersList = new ArrayList<>();
        m3uaServersList.add(m3uaMapper.toEntityServer(m3uaSocketsDTO));

        Mockito.when(m3uaSocketsRepository.findBySs7M3uaId(anyInt())).thenReturn(m3uaServersList);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getM3uaServers(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(m3uaSocketsRepository.findBySs7M3uaId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getM3uaServers(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaSocketsRepository).findBySs7M3uaId(anyInt());
        response = ss7GatewaysController.getM3uaServers(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createM3uaSocketsTest() {
        M3uaSocketsDTO newM3uaSocketsDTO = Utils.newM3uaSocketsMock();

        M3uaSocketsDTO m3uaSocketsDTO = Utils.getM3uaSocketsMock();
        M3uaSockets m3uaSocketsEntity = m3uaMapper.toEntityServer(m3uaSocketsDTO);

        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createM3uaServer(newM3uaSocketsDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaSocketsRepository).save(any());
        response = ss7GatewaysController.createM3uaServer(newM3uaSocketsDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateM3uaSocketsTest() throws Exception {
        M3uaSocketsDTO currentM3uaSocketsDTO = Utils.getM3uaSocketsMock();
        M3uaSockets currentM3uaSocketsEntity = m3uaMapper.toEntityServer(currentM3uaSocketsDTO);

        M3uaSocketsDTO newM3uaSocketsDTO = Utils.getM3uaSocketsMock();
        newM3uaSocketsDTO.setName("socket test");
        M3uaSockets m3uaSocketsEntityResponse = m3uaMapper.toEntityServer(newM3uaSocketsDTO);

        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (M3uaSocketsDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("socket test", apiResponse.getName());

        newM3uaSocketsDTO.setEnabled(0);
        currentM3uaSocketsEntity.setEnabled(0);
        m3uaSocketsEntityResponse.setEnabled(0);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.OK);
        apiResponse = (M3uaSocketsDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("socket test", apiResponse.getName());

        newM3uaSocketsDTO.setEnabled(1);
        currentM3uaSocketsEntity.setEnabled(0);
        m3uaSocketsEntityResponse.setEnabled(1);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        Mockito.when(ss7GatewaysRepository.existsByNetworkIdAndEnabled(anyInt(), anyInt())).thenReturn(false);
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newM3uaSocketsDTO.setEnabled(1);
        currentM3uaSocketsEntity.setEnabled(0);
        m3uaSocketsEntityResponse.setEnabled(1);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        Mockito.when(ss7GatewaysRepository.existsByNetworkIdAndEnabled(anyInt(), anyInt())).thenReturn(true);
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.OK);

        newM3uaSocketsDTO.setEnabled(2);
        currentM3uaSocketsEntity.setEnabled(0);
        m3uaSocketsEntityResponse.setEnabled(2);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        newM3uaSocketsDTO.setEnabled(1);
        currentM3uaSocketsEntity.setEnabled(1);
        m3uaSocketsEntityResponse.setEnabled(1);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        newM3uaSocketsDTO.setEnabled(0);
        currentM3uaSocketsEntity.setEnabled(1);
        m3uaSocketsEntityResponse.setEnabled(0);
        Mockito.when(m3uaSocketsRepository.findNetworkIdById(anyInt())).thenReturn(3);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.when(m3uaSocketsRepository.save(any())).thenReturn(m3uaSocketsEntityResponse);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.OK);

        currentM3uaSocketsEntity.setEnabled(1);
        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaSocketsRepository).findById(anyInt());
        response = ss7GatewaysController.updateM3uaServer(newM3uaSocketsDTO, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteM3uaSocketsTest() {
        M3uaSocketsDTO currentM3uaSocketsDTO = Utils.getM3uaSocketsMock();
        M3uaSockets currentM3uaSocketsEntity = m3uaMapper.toEntityServer(currentM3uaSocketsDTO);

        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.doNothing().when(m3uaSocketsRepository).delete(currentM3uaSocketsEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteM3uaServer(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteM3uaServer(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(m3uaSocketsRepository.findById(anyInt())).thenReturn(currentM3uaSocketsEntity);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaSocketsRepository).delete(currentM3uaSocketsEntity);
        response = ss7GatewaysController.deleteM3uaServer(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getM3uaAssociationTest() {
        M3uaAssociationsDTO m3uaAssociationsDTO = Utils.getM3uaAssociationsDTO();
        List<M3uaAssociationsDTO> m3uaAssociations = new ArrayList<>();
        m3uaAssociations.add(m3uaAssociationsDTO);

        Mockito.when(m3uaAssociationsRepository.fetchM3uaAssociations(anyInt())).thenReturn(m3uaAssociations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getM3uaAssociation(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(m3uaAssociationsRepository.fetchM3uaAssociations(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getM3uaAssociation(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaAssociationsRepository).fetchM3uaAssociations(anyInt());
        response = ss7GatewaysController.getM3uaAssociation(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createM3uaAssociationTest() {
        M3uaAssociationsDTO m3uaAssociationsDTO = Utils.getM3uaAssociationsDTO();
        M3uaAssociations m3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsDTO);
        M3uaAssociationsDTO newM3uaAssoc = Utils.newM3uaAssociationsDTO();

        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createM3uaAssociation(newM3uaAssoc);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaAssociationsRepository).save(any());
        response = ss7GatewaysController.createM3uaAssociation(newM3uaAssoc);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateM3uaAssociationTest() throws Exception {
        M3uaAssociationsDTO m3uaAssociationsDTO = Utils.getM3uaAssociationsDTO();
        M3uaAssociations currentM3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsDTO);

        M3uaAssociationsDTO m3uaAssociationsUpdated = Utils.getM3uaAssociationsDTO();
        m3uaAssociationsUpdated.setName("updating name");
        M3uaAssociations m3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsUpdated);

        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (M3uaAssociationsDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("updating name", apiResponse.getName());

        m3uaAssociationsUpdated.setEnabled(1);
        m3uaMapper.updateEntityFromAssociationsDTO(m3uaAssociationsUpdated, m3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        Mockito.when(ss7GatewaysRepository.existsByNetworkIdAndEnabled(anyInt(), anyInt())).thenReturn(true);
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        apiResponse = (M3uaAssociationsDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("updating name", apiResponse.getName());

        currentM3uaAssociations.setEnabled(0);
        m3uaAssociationsUpdated.setEnabled(1);
        m3uaMapper.updateEntityFromAssociationsDTO(m3uaAssociationsUpdated, m3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        Mockito.when(ss7GatewaysRepository.existsByNetworkIdAndEnabled(anyInt(), anyInt())).thenReturn(false);
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        m3uaAssociationsUpdated.setEnabled(0);
        currentM3uaAssociations.setEnabled(1);
        m3uaMapper.updateEntityFromAssociationsDTO(m3uaAssociationsUpdated, m3uaAssociations);
        Mockito.when(ss7GatewaysRepository.existsByNetworkIdAndEnabled(anyInt(), anyInt())).thenReturn(true);
        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.OK);

        m3uaAssociationsUpdated.setEnabled(1);
        currentM3uaAssociations.setEnabled(1);
        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        m3uaAssociationsUpdated.setEnabled(1);
        currentM3uaAssociations.setEnabled(0);
        m3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsUpdated);
        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        m3uaAssociationsUpdated.setEnabled(2);
        currentM3uaAssociations.setEnabled(0);
        m3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsUpdated);
        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.when(m3uaAssociationsRepository.save(any())).thenReturn(m3uaAssociations);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaAssociationsRepository).save(any());
        response = ss7GatewaysController.updateM3uaAssociation(m3uaAssociationsUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteM3uaAssociationTest() {
        M3uaAssociationsDTO m3uaAssociationsDTO = Utils.getM3uaAssociationsDTO();
        M3uaAssociations currentM3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsDTO);

        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.doNothing().when(m3uaAssociationsRepository).delete(currentM3uaAssociations);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteM3uaAssociation(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(null);
        Mockito.doNothing().when(m3uaAssociationsRepository).delete(currentM3uaAssociations);
        response = ss7GatewaysController.deleteM3uaAssociation(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(currentM3uaAssociations);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaAssociationsRepository).delete(any());
        response = ss7GatewaysController.deleteM3uaAssociation(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getM3uaAppServerTest() {
        M3uaApplicationServerDTO m3uaApplicationServer = Utils.getM3uaApplicationServer();
        M3uaApplicationServerDTO m3uaApplicationServer2 = Utils.getM3uaApplicationServer();
        m3uaApplicationServer2.setAspFactories(new ArrayList<>(Arrays.asList(3, 4)));

        List<M3uaApplicationServerDTO> m3uaApplicationServers = new ArrayList<>();
        m3uaApplicationServers.add(m3uaApplicationServer);
        m3uaApplicationServers.add(m3uaApplicationServer2);

        Mockito.when(m3uaApplicationServerRepository.fetchM3uaAppServer(anyInt())).thenReturn(m3uaApplicationServers);
        Mockito.when(m3uaAssAppServersRepository.fetchAssAppServers(anyInt())).thenReturn(m3uaApplicationServer.getAspFactories())
                .thenReturn(m3uaApplicationServer2.getAspFactories());
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getM3uaAppServer(1);
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(Objects.requireNonNull(response.getBody()).data());

        Mockito.when(m3uaApplicationServerRepository.fetchM3uaAppServer(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getM3uaAppServer(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaApplicationServerRepository).fetchM3uaAppServer(anyInt());
        response = ss7GatewaysController.getM3uaAppServer(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createM3uaAppServerTest() {
        M3uaApplicationServerDTO m3uaApplicationServer = Utils.newM3uaApplicationServer();
        M3uaApplicationServer m3uaAppServer = m3uaMapper.toEntityAppServer(m3uaApplicationServer);

        List<M3uaAssAppServers> m3uaAssAppServersMock = new ArrayList<>();

        Mockito.when(m3uaApplicationServerRepository.save(any())).thenReturn(m3uaAppServer);
        Mockito.when(m3uaAssAppServersRepository.save(any())).thenReturn(m3uaAssAppServersMock);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createM3uaAppServer(m3uaApplicationServer);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaApplicationServerRepository).save(any());
        response = ss7GatewaysController.createM3uaAppServer(m3uaApplicationServer);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateM3uaAppServerTest() {
        M3uaApplicationServerDTO m3uaApplicationServerUpdated = Utils.getM3uaApplicationServer();
        M3uaApplicationServer m3uaAppServerUpdated = m3uaMapper.toEntityAppServer(m3uaApplicationServerUpdated);
        m3uaAppServerUpdated.setName("updating name");

        M3uaApplicationServerDTO m3uaApplicationServer = Utils.getM3uaApplicationServer();
        M3uaApplicationServer m3uaAppServer = m3uaMapper.toEntityAppServer(m3uaApplicationServer);

        List<M3uaAssAppServers> m3uaAssAppServerList = new ArrayList<>();
        M3uaAssAppServers m3uaAssAppServersMock = new M3uaAssAppServers();
        m3uaAssAppServersMock.setId(1);
        m3uaAssAppServersMock.setAspId(1);
        m3uaAssAppServersMock.setApplicationServerId(1);
        m3uaAssAppServerList.add(m3uaAssAppServersMock);

        Mockito.when(m3uaApplicationServerRepository.findById(anyInt())).thenReturn(m3uaAppServer);
        Mockito.when(m3uaApplicationServerRepository.save(any())).thenReturn(m3uaAppServerUpdated);
        Mockito.when(m3uaAssAppServersRepository.findByApplicationServerId(anyInt())).thenReturn(m3uaAssAppServerList);
        Mockito.doNothing().when(m3uaAssAppServersRepository).deleteAll(anyList());
        Mockito.when(m3uaAssAppServersRepository.saveAll(anyList())).thenReturn(null);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateM3uaAppServer(m3uaApplicationServerUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (M3uaApplicationServerDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("updating name", apiResponse.getName());

        Mockito.when(m3uaApplicationServerRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateM3uaAppServer(m3uaApplicationServerUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        M3uaAssociationsDTO m3uaAssociationsDTO = Utils.getM3uaAssociationsDTO();
        M3uaAssociations m3uaAssociations = m3uaMapper.toEntityAssociation(m3uaAssociationsDTO);
        Mockito.when(m3uaAssociationsRepository.findById(anyInt())).thenReturn(m3uaAssociations);

        Mockito.when(m3uaApplicationServerRepository.findById(anyInt())).thenReturn(m3uaAppServer);
        Mockito.when(m3uaApplicationServerRepository.save(any())).thenReturn(m3uaAppServerUpdated);
        Mockito.when(m3uaAssAppServersRepository.getAsFunctionality(anyInt(), anyInt())).thenReturn("IPSP-SERVER");
        Mockito.when(m3uaAssAppServersRepository.findByApplicationServerId(anyInt())).thenReturn(m3uaAssAppServerList);
        Mockito.doNothing().when(m3uaAssAppServersRepository).deleteAll(anyList());
        Mockito.when(m3uaAssAppServersRepository.saveAll(anyList())).thenReturn(null);
        response = ss7GatewaysController.updateM3uaAppServer(m3uaApplicationServerUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(m3uaAssAppServersRepository.getAsFunctionality(anyInt(), anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateM3uaAppServer(m3uaApplicationServerUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteM3uaAppServerTest() {
        M3uaApplicationServerDTO m3uaApplicationServer = Utils.getM3uaApplicationServer();
        M3uaApplicationServer m3uaAppServer = m3uaMapper.toEntityAppServer(m3uaApplicationServer);

        List<M3uaAssAppServers> m3uaAssAppServerList = new ArrayList<>();
        M3uaAssAppServers m3uaAssAppServersMock = new M3uaAssAppServers();
        m3uaAssAppServersMock.setId(1);
        m3uaAssAppServersMock.setAspId(1);
        m3uaAssAppServersMock.setApplicationServerId(1);
        m3uaAssAppServerList.add(m3uaAssAppServersMock);

        Mockito.when(m3uaApplicationServerRepository.findById(anyInt())).thenReturn(m3uaAppServer);
        Mockito.when(m3uaAssAppServersRepository.findByApplicationServerId(anyInt())).thenReturn(m3uaAssAppServerList);
        Mockito.doNothing().when(m3uaApplicationServerRepository).delete(m3uaAppServer);
        Mockito.doNothing().when(m3uaAssAppServersRepository).deleteAll(m3uaAssAppServerList);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteM3uaAppServer(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.when(m3uaApplicationServerRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteM3uaAppServer(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaApplicationServerRepository).findById(anyInt());
        response = ss7GatewaysController.deleteM3uaAppServer(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getM3uaRoutesTest() {
        M3uaRoutesDTO m3uaRoutesDTO = Utils.getM3uaRoutesDTO();

        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2));

        Mockito.when(m3uaApplicationServerRepository.fetchM3uaAppServerId(anyInt())).thenReturn(list);
        Mockito.when(m3uaRoutesRepository.fetchM3uaRoutes(anyList())).thenReturn(List.of(m3uaRoutesDTO));
        Mockito.when(m3uaAppServersRouteRepository.fetchAppServersRoute(anyInt())).thenReturn(List.of(1,2));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getM3uaRoutes(1);
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(Objects.requireNonNull(response.getBody()).data());

        Mockito.when(m3uaRoutesRepository.fetchM3uaRoutes(anyList())).thenReturn(null);
        response = ss7GatewaysController.getM3uaRoutes(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRoutesRepository).fetchM3uaRoutes(anyList());
        response = ss7GatewaysController.getM3uaRoutes(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createM3uaRoutesTest() {
        M3uaDTO m3uaMock = Utils.getM3uatDtoMock();
        M3ua m3uaEntityMock = m3uaMapper.toEntity(m3uaMock);

        M3uaRoutesDTO newM3uaRoutesDTO = Utils.newM3uaRoutesDTO();
        M3uaRoutesDTO m3uaRoutesDTO = Utils.getM3uaRoutesDTO();
        M3uaRoutes m3uaRoutes = m3uaMapper.toEntityRoutes(m3uaRoutesDTO);

        Mockito.when(m3uaRepository.findById(anyInt())).thenReturn(m3uaEntityMock);
        Mockito.when(m3uaRoutesRepository.save(any())).thenReturn(m3uaRoutes);
        Mockito.when(m3uaApplicationServerRepository.saveAll(anyList())).thenReturn(null);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createM3uaRoutes(newM3uaRoutesDTO,1);
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(Objects.requireNonNull(response.getBody()).data());

        Mockito.when(m3uaRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.createM3uaRoutes(newM3uaRoutesDTO,1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRepository).findById(anyInt());
        response = ss7GatewaysController.createM3uaRoutes(newM3uaRoutesDTO,1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateM3uaRoutesTest() {
        M3uaRoutesDTO m3uaRoutesDTOUpdated = Utils.getM3uaRoutesDTO();
        m3uaRoutesDTOUpdated.setOriginationPointCode(0);
        M3uaRoutes m3uaRoutesResponse = m3uaMapper.toEntityRoutes(m3uaRoutesDTOUpdated);

        M3uaRoutesDTO m3uaRoutesDTO = Utils.getM3uaRoutesDTO();
        M3uaRoutes currentM3uaRoutes = m3uaMapper.toEntityRoutes(m3uaRoutesDTO);

        M3uaAppServersRoutes m3uaAppServersRoutes = new M3uaAppServersRoutes();
        m3uaAppServersRoutes.setId(1);
        m3uaAppServersRoutes.setRouteId(1);
        m3uaAppServersRoutes.setApplicationServerId(1);

        Mockito.when(m3uaRoutesRepository.findById(anyInt())).thenReturn(currentM3uaRoutes);
        Mockito.when(m3uaRoutesRepository.save(any())).thenReturn(m3uaRoutesResponse);
        Mockito.when(m3uaAppServersRouteRepository.findByRouteId(anyInt())).thenReturn(List.of(m3uaAppServersRoutes));
        Mockito.doNothing().when(m3uaAppServersRouteRepository).deleteAll(anyList());
        Mockito.when(m3uaAppServersRouteRepository.saveAll(anyList())).thenReturn(null);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateM3uaRoutes(m3uaRoutesDTOUpdated,1);
        checkAssertions(response, HttpStatus.OK);
        assertNotNull(Objects.requireNonNull(response.getBody()).data());
        var apiResponse = (M3uaRoutesDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals(0, apiResponse.getOriginationPointCode());

        Mockito.when(m3uaRoutesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateM3uaRoutes(m3uaRoutesDTOUpdated,1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaRoutesRepository).findById(anyInt());
        response = ss7GatewaysController.updateM3uaRoutes(m3uaRoutesDTOUpdated,1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteM3uaRoutesTest() {
        M3uaRoutesDTO m3uaRoutesDTO= Utils.getM3uaRoutesDTO();
        M3uaRoutes currentM3uaRoutes = m3uaMapper.toEntityRoutes(m3uaRoutesDTO);

        M3uaAppServersRoutes m3uaAppServersRoutes = new M3uaAppServersRoutes();
        m3uaAppServersRoutes.setId(1);
        m3uaAppServersRoutes.setRouteId(1);
        m3uaAppServersRoutes.setApplicationServerId(1);

        Mockito.when(m3uaRoutesRepository.findById(anyInt())).thenReturn(currentM3uaRoutes);
        Mockito.when(m3uaAppServersRouteRepository.findByRouteId(anyInt())).thenReturn(List.of(m3uaAppServersRoutes));
        Mockito.doNothing().when(m3uaAppServersRouteRepository).deleteAll(anyList());
        Mockito.doNothing().when(m3uaRoutesRepository).delete(any());
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteM3uaRoutes(1);
        checkAssertions(response, HttpStatus.OK, "DELETE");

        Mockito.doThrow(new RuntimeException("Test Exception")).when(m3uaAppServersRouteRepository).deleteAll(anyList());
        response = ss7GatewaysController.deleteM3uaRoutes(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(m3uaRoutesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteM3uaRoutes(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);
    }
}