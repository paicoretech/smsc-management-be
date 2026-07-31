package com.smsc.management.app.ss7.controller;

import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.mapper.Ss7GatewaysMapper;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.service.M3uaService;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
import com.smsc.management.app.ss7.utilsTest.Utils;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.security.JwtService;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.UtilsBase;
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

import java.util.List;
import java.util.Objects;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;

class Ss7GatewaysControllerTest extends BaseIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Ss7GatewaysController ss7GatewaysController;

    @Autowired
    private Ss7GatewaysMapper ss7GatewaysMapper;

    @MockBean
    private Ss7GatewaysRepository ss7GatewaysRepository;

    @MockBean
    private M3uaService m3uaService;

    @MockBean
    private SequenceNetworksIdRepository sequenceNetworksIdRepository;

    @MockBean
    private UtilsBase utilsBase;

    @MockBean
    private ObjectSs7Service objectSs7Service;

    @MockBean
    private JwtService jwtService;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "ss7_gateways");
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getListGateway() {
        Ss7GatewaysDTO ss7GatewaysDTO = Utils.getSs7GatewaysDTO();
        Ss7Gateways ss7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);

        Mockito.when(ss7GatewaysRepository.findByEnabledNotAndAllowedTraffic(Constants.DELETED_ENABLED_STATUS, Constants.SS7_ALLOWED_TRAFFIC)).thenReturn(List.of(ss7Gateways));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.listGateway();
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(ss7GatewaysRepository).findByEnabledNotAndAllowedTraffic(anyInt(), anyBoolean());
        response = ss7GatewaysController.listGateway();
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getListGatewayIpSmGw() {
        Ss7GatewaysDTO ss7GatewaysDTO = Utils.getSs7GatewaysDTO();
        Ss7Gateways ss7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);

        Mockito.when(ss7GatewaysRepository.findAllForIpSmGw(Constants.DELETED_ENABLED_STATUS))
                .thenReturn(List.of(ss7Gateways));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.listGatewayIpSmGw();
        checkAssertions(response, HttpStatus.OK);


        Mockito.doThrow(new RuntimeException("Test Exception"))
                .when(ss7GatewaysRepository)
                .findAllForIpSmGw(Mockito.anyInt());
        response = ss7GatewaysController.listGatewayIpSmGw();

        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getGWByExternalIDTest() {
        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenReturn(new Ss7Gateways());
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSs7GatewayByExternalId("5");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(Ss7GatewaysDTO.class, response.getBody().data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getGWByExternalIDNotFoundTest() {
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSs7GatewayByExternalId("5");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getGWByExternalIDAnyExceptionTest() {
        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenThrow(new RuntimeException("RuntimeException"));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSs7GatewayByExternalId("5");
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createTest() {
        Ss7GatewaysDTO newSs7GatewaysDTO = Utils.newSs7GatewaysDTO();
        SequenceNetworksId newSeqGwMock = new SequenceNetworksId("GW");

        Ss7GatewaysDTO ss7GatewaysDTO = Utils.getSs7GatewaysDTO();
        Ss7Gateways ss7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);

        Mockito.when(sequenceNetworksIdRepository.save(any())).thenReturn(newSeqGwMock);
        Mockito.when(ss7GatewaysRepository.save(any())).thenReturn(ss7Gateways);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.create(newSs7GatewaysDTO);
        checkAssertions(response, HttpStatus.OK);

        Ss7GatewaysDTO newSs7GatewaysDTOWithApi = Utils.newSs7GatewaysDTO();
        newSs7GatewaysDTOWithApi.setApiEnabled(true);
        final String generatedToken = "test.jwt.token";
        Mockito.when(jwtService.generateToken()).thenReturn(generatedToken);
        Ss7Gateways ss7GatewaysWithToken = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);
        Mockito.when(ss7GatewaysRepository.save(any())).thenReturn(ss7GatewaysWithToken);
        ResponseEntity<ApiResponse> responseWithApi = ss7GatewaysController.create(newSs7GatewaysDTOWithApi);
        checkAssertions(responseWithApi, HttpStatus.OK);
        assertInstanceOf(ApiResponse.class, responseWithApi.getBody());
        assertInstanceOf(Ss7GatewaysDTO.class, responseWithApi.getBody().data());
        Mockito.verify(jwtService, Mockito.times(1)).generateToken();
        Mockito.verify(ss7GatewaysRepository, Mockito.atLeastOnce()).save(any());

        Mockito.doThrow(new RuntimeException("Test Exception")).when(ss7GatewaysRepository).save(any());
        response = ss7GatewaysController.create(newSs7GatewaysDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateByPkTest() {
        update(Boolean.TRUE);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateByExternalIdTest() {
        update(Boolean.FALSE);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getGatewaysByNetworkId() {
        Ss7GatewaysDTO ss7GatewaysDTO = Utils.getSs7GatewaysDTO();
        Ss7Gateways currentSs7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);

        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(currentSs7Gateways);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getGateway(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getGateway(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(ss7GatewaysRepository).findById(anyInt());
        response = ss7GatewaysController.getGateway(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    void update(boolean updateByPk) {

        Ss7GatewaysDTO ss7GatewaysDTOUpdated = Utils.getSs7GatewaysDTO();
        ss7GatewaysDTOUpdated.setName("updating Name");
        ss7GatewaysDTOUpdated.setExternalId("10");
        Ss7Gateways ss7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTOUpdated);

        Ss7GatewaysDTO ss7GatewaysDTO = Utils.getSs7GatewaysDTO();
        ss7GatewaysDTO.setExternalId("10");
        Ss7Gateways currentSs7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);
        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(currentSs7Gateways);
        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenReturn(currentSs7Gateways);
        Mockito.when(ss7GatewaysRepository.save(any())).thenReturn(ss7Gateways);
        ResponseEntity<ApiResponse> response = updateByPk ? ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1) :
                ss7GatewaysController.updateByExternalId(ss7GatewaysDTOUpdated, "10");
        checkAssertions(response, HttpStatus.OK);
        var apiResponse = (Ss7GatewaysDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("updating Name", apiResponse.getName());

        Mockito.doThrow(new RuntimeException("test exception")).when(m3uaService).propagateStatusForM3ua(anyInt(), anyInt());
        response = updateByPk ? ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1) :
                ss7GatewaysController.updateByExternalId(ss7GatewaysDTOUpdated, "10");
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.doNothing().when(m3uaService).propagateStatusForM3ua(anyInt(), anyInt());
        ss7GatewaysDTO = Utils.getSs7GatewaysDTO();
        currentSs7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);
        currentSs7Gateways.setEnabled(Constants.ACTIVE_ENABLED_STATUS);
        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(currentSs7Gateways);
        response = ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        apiResponse = (Ss7GatewaysDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("updating Name", apiResponse.getName());

        Mockito.doNothing().when(utilsBase).removeInRedis(anyString(), anyString());
        Mockito.doNothing().when(utilsBase).sendNotificationSocket(anyString(), anyString());
        ss7GatewaysDTOUpdated.setEnabled(Constants.DELETED_ENABLED_STATUS);
        ss7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTOUpdated);
        Mockito.when(ss7GatewaysRepository.save(any())).thenReturn(ss7Gateways);
        response = ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        apiResponse = (Ss7GatewaysDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertEquals("updating Name", apiResponse.getName());

        currentSs7Gateways.setEnabled(Constants.DELETED_ENABLED_STATUS);
        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(currentSs7Gateways);
        response = ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1);
        checkAssertions(response, HttpStatus.BAD_REQUEST);

        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(ss7GatewaysRepository).findById(anyInt());
        response = ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);

        ss7GatewaysDTOUpdated = Utils.getSs7GatewaysDTO();
        ss7GatewaysDTOUpdated.setApiEnabled(true);

        currentSs7Gateways = ss7GatewaysMapper.toEntity(ss7GatewaysDTO);
        currentSs7Gateways.setApiEnabled(false);
        currentSs7Gateways.setAppToken(null);

        Mockito.reset(ss7GatewaysRepository);
        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(currentSs7Gateways);
        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenReturn(currentSs7Gateways);
        Mockito.when(ss7GatewaysRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        response = ss7GatewaysController.update(ss7GatewaysDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);

        apiResponse = (Ss7GatewaysDTO) Objects.requireNonNull(response.getBody()).data();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isApiEnabled());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void generateGatewayTokenByNetworkId() throws Exception {
        Ss7GatewaysDTO baseDto = Utils.getSs7GatewaysDTO();
        Ss7Gateways entity = ss7GatewaysMapper.toEntity(baseDto);
        entity.setNetworkId(1234);

        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(entity);
        Mockito.when(jwtService.generateToken()).thenReturn("test.jwt.token");
        Mockito.when(ss7GatewaysRepository.save(any())).thenReturn(entity);
        Mockito.doNothing().when(objectSs7Service).updateOrCreateJsonInRedis(anyInt());

        ResponseEntity<ApiResponse> response = ss7GatewaysController.generateGatewayToken(1234);
        checkAssertions(response, HttpStatus.OK);
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(Ss7GatewaysDTO.class, response.getBody().data());

        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.generateGatewayToken(9999);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(ss7GatewaysRepository.findById(anyInt())).thenThrow(new RuntimeException("boom"));
        response = ss7GatewaysController.generateGatewayToken(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void generateGatewayTokenByExternalId() {
        Ss7GatewaysDTO dto = Utils.getSs7GatewaysDTO();
        dto.setExternalId("1");
        Ss7Gateways entity = ss7GatewaysMapper.toEntity(dto);
        entity.setExternalId("2");
        entity.setNetworkId(777);

        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenReturn(entity);
        Mockito.when(jwtService.generateToken()).thenReturn("test.jwt.token");
        Mockito.when(ss7GatewaysRepository.save(any())).thenReturn(entity);

        ResponseEntity<ApiResponse> response = ss7GatewaysController.generateGatewayTokenByExternalId("1");
        checkAssertions(response, HttpStatus.OK);
        assertInstanceOf(ApiResponse.class, response.getBody());
        assertInstanceOf(Ss7GatewaysDTO.class, response.getBody().data());

        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenReturn(null);
        response = ss7GatewaysController.generateGatewayTokenByExternalId("1234");
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(ss7GatewaysRepository.findByExternalId(anyString())).thenThrow(new RuntimeException("boom"));
        response = ss7GatewaysController.generateGatewayTokenByExternalId("1234");
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
