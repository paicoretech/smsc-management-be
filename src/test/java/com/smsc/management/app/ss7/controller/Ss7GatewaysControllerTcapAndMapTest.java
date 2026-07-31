package com.smsc.management.app.ss7.controller;

import com.smsc.management.app.ss7.dto.MapDTO;
import com.smsc.management.app.ss7.dto.TcapDTO;
import com.smsc.management.app.ss7.mapper.MapMapper;
import com.smsc.management.app.ss7.mapper.TcapMapper;
import com.smsc.management.app.ss7.model.entity.Map;
import com.smsc.management.app.ss7.model.entity.Tcap;
import com.smsc.management.app.ss7.model.repository.MapRepository;
import com.smsc.management.app.ss7.model.repository.TcapRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

class Ss7GatewaysControllerTcapAndMapTest extends BaseIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Ss7GatewaysController ss7GatewaysController;

    @Autowired
    TcapMapper tcapMapper;

    @Autowired
    MapMapper mapMapper;

    @MockBean
    TcapRepository tcapRepository;

    @MockBean
    MapRepository mapRepository;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "map");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "tcap");
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void testGetTcap() {
        TcapDTO tcapDTO = Utils.getTcapDTOMock();
        Tcap tcap = tcapMapper.toEntity(tcapDTO);

        Mockito.when(tcapRepository.findByNetworkId(anyInt())).thenReturn(tcap);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getTcap(3);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

        Mockito.when(tcapRepository.findByExternalId(anyString())).thenReturn(tcap);
        response = ss7GatewaysController.getTcapByExternalId("003");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

        Mockito.when(tcapRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getTcap(3);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        ApiResponse apiResponse = response.getBody();
        assertNull(apiResponse.data());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(tcapRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.getTcap(3);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createTcap() {

        TcapDTO tcapDTO = Utils.getTcapDTOMock();
        tcapDTO.setId(0);

        TcapDTO tcapDTOResponse = Utils.getTcapDTOMock();
        Tcap tcap = tcapMapper.toEntity(tcapDTOResponse);

        Mockito.when(tcapRepository.save(any())).thenReturn(tcap);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createTcap(tcapDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        TcapDTO apiResponse = (TcapDTO) response.getBody().data();
        assertEquals(2, apiResponse.getId());

        tcapDTO.setInvokeTimeout(100001);
        response = ss7GatewaysController.createTcap(tcapDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        response = ss7GatewaysController.createTcap(null);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateTcap() {
        TcapDTO tcapDTO = Utils.getTcapDTOMock();
        Tcap tcapEntity = tcapMapper.toEntity(tcapDTO);
        tcapDTO.setInvokeTimeout(123456);

        TcapDTO tcapDTOResponse = Utils.getTcapDTOMock();
        tcapDTOResponse.setInvokeTimeout(123456);
        Tcap tcapEntityResponse = tcapMapper.toEntity(tcapDTOResponse);

        Mockito.when(tcapRepository.findById(anyInt())).thenReturn(tcapEntity);
        Mockito.when(tcapRepository.save(any())).thenReturn(tcapEntityResponse);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateTcap(tcapDTO, 2);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        TcapDTO apiResponse = (TcapDTO) response.getBody().data();
        assertEquals(2, apiResponse.getId());
        assertEquals(123456, apiResponse.getInvokeTimeout());

        Mockito.when(tcapRepository.findByExternalId(anyString())).thenReturn(tcapEntity);
        response = ss7GatewaysController.updateTcapByExternalId(tcapDTO, "002");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Mockito.when(tcapRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateTcap(tcapDTO, 2);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(tcapRepository).findById(anyInt());
        response = ss7GatewaysController.updateTcap(tcapDTO, 2);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteTcap() {
        TcapDTO tcapDTO = Utils.getTcapDTOMock();
        Tcap tcapEntity = tcapMapper.toEntity(tcapDTO);

        Mockito.when(tcapRepository.findByNetworkId(anyInt())).thenReturn(tcapEntity);
        Mockito.doNothing().when(tcapRepository).delete(tcapEntity);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteTcap(2);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        Mockito.when(tcapRepository.findByExternalId(anyString())).thenReturn(tcapEntity);
        response = ss7GatewaysController.deleteTcapByExternalId("002");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Mockito.when(tcapRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteTcap(2);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(tcapRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.deleteTcap(2);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void testGetMap() {
        MapDTO mapDTO = Utils.getMapDTOMock();
        Map map = mapMapper.toEntity(mapDTO);

        Mockito.when(mapRepository.findByNetworkId(anyInt())).thenReturn(map);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getMap(3);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

        Mockito.when(mapRepository.findByExternalId(anyString())).thenReturn(map);
        response = ss7GatewaysController.getMapByExternalId("003");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

        Mockito.when(mapRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getMap(3);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        ApiResponse apiResponse = response.getBody();
        assertNull(apiResponse.data());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(mapRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.getMap(3);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createMap() {

        MapDTO mapDTO = Utils.getMapDTOMock();
        mapDTO.setId(0);

        MapDTO mapDTOResponse = Utils.getMapDTOMock();
        Map map = mapMapper.toEntity(mapDTOResponse);

        Mockito.when(mapRepository.save(any())).thenReturn(map);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createMap(mapDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        MapDTO apiResponse = (MapDTO) response.getBody().data();
        assertEquals(2, apiResponse.getId());

        response = ss7GatewaysController.createMap(null);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateMap() {

        MapDTO mapDTO = Utils.getMapDTOMock();
        mapDTO.setSriServiceOpCode(35);
        Map newMap = mapMapper.toEntity(mapDTO);

        MapDTO mapDTOResponse = Utils.getMapDTOMock();
        Map map = mapMapper.toEntity(mapDTOResponse);

        Mockito.when(mapRepository.findById(anyInt())).thenReturn(map);
        Mockito.when(mapRepository.save(any())).thenReturn(newMap);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateMap(mapDTO, 2);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        MapDTO apiResponse = (MapDTO) response.getBody().data();
        assertEquals(2, apiResponse.getId());
        assertEquals(35, apiResponse.getSriServiceOpCode());

        Mockito.when(mapRepository.findByExternalId(anyString())).thenReturn(map);
        response = ss7GatewaysController.updateMapByExternalId(mapDTO, "002");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Mockito.when(mapRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateMap(mapDTO, 2);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(mapRepository).findById(anyInt());
        response = ss7GatewaysController.updateMap(mapDTO, 2);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteMap() {

        MapDTO mapDTO = Utils.getMapDTOMock();
        Map map = mapMapper.toEntity(mapDTO);

        Mockito.when(mapRepository.findByNetworkId(anyInt())).thenReturn(map);
        Mockito.doNothing().when(mapRepository).delete(map);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteMap(2);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        Mockito.when(mapRepository.findByExternalId(anyString())).thenReturn(map);
        response = ss7GatewaysController.deleteMapByExternalId("002");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        Mockito.when(mapRepository.findByNetworkId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteMap(2);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(mapRepository).findByNetworkId(anyInt());
        response = ss7GatewaysController.deleteMap(2);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertNull(response.getBody().data());
    }
}