package com.smsc.management.app.ss7.controller;

import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.mapper.SccpMapper;
import com.smsc.management.app.ss7.model.entity.SccpLongMessageRules;
import com.smsc.management.app.ss7.model.repository.SccpLongMessageRulesRepository;
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

import java.util.List;
import java.util.Objects;

import static com.smsc.management.app.ss7.utilsTest.Utils.checkAssertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

class Ss7GatewaysControllerSccpLongMessageRulesTest extends BaseIntegrationTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Ss7GatewaysController ss7GatewaysController;

    @Autowired
    SccpMapper sccpMapper;

    @MockBean
    SccpLongMessageRulesRepository sccpLongMessageRulesRepository;

    @MockBean
    ObjectSs7Service objectSs7Service;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "sccp_long_message_rules");
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void getSccpLongMessageRulesTest() {
        SccpLongMessageRulesDTO sccpLongMessageRulesDTO = Utils.getSccpLongMessageRulesDTOMock();

        Mockito.when(sccpLongMessageRulesRepository.fetchLongMessageRulesBySccpId(anyInt())).thenReturn(List.of(sccpLongMessageRulesDTO));
        ResponseEntity<ApiResponse> response = ss7GatewaysController.getSccpLongMessageRules(1);
        checkAssertions(response, HttpStatus.OK);

        Mockito.when(sccpLongMessageRulesRepository.fetchLongMessageRulesBySccpId(anyInt())).thenReturn(null);
        response = ss7GatewaysController.getSccpLongMessageRules(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpLongMessageRulesRepository).fetchLongMessageRulesBySccpId(anyInt());
        response = ss7GatewaysController.getSccpLongMessageRules(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void createSccpLongMessageRulesTest() {
        SccpLongMessageRulesDTO newSccpLongMessageRulesDTO = Utils.newSccpLongMessageRulesDTOMock();

        SccpLongMessageRulesDTO sccpLongMessageRulesDTO = Utils.getSccpLongMessageRulesDTOMock();
        SccpLongMessageRules savedSccpLongMessageRules = sccpMapper.toEntityLongMessageRules(sccpLongMessageRulesDTO);

        Mockito.when(sccpLongMessageRulesRepository.save(any())).thenReturn(savedSccpLongMessageRules);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.createSccpLongMessageRules(newSccpLongMessageRulesDTO);
        checkAssertions(response, HttpStatus.OK);

        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpLongMessageRulesRepository).save(any());
        response = ss7GatewaysController.createSccpLongMessageRules(newSccpLongMessageRulesDTO);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void updateSccpLongMessageRulesTest() {
        SccpLongMessageRulesDTO sccpLongMessageRulesDTOUpdated = Utils.getSccpLongMessageRulesDTOMock();
        sccpLongMessageRulesDTOUpdated.setLongMessageRuleType("LUDT_ENABLED");
        SccpLongMessageRules sccpLongMessageRules = sccpMapper.toEntityLongMessageRules(sccpLongMessageRulesDTOUpdated);

        SccpLongMessageRulesDTO sccpLongMessageRulesDTO = Utils.getSccpLongMessageRulesDTOMock();
        SccpLongMessageRules currentSccpLongMessageRules = sccpMapper.toEntityLongMessageRules(sccpLongMessageRulesDTO);

        Mockito.when(sccpLongMessageRulesRepository.findById(anyInt())).thenReturn(currentSccpLongMessageRules);
        Mockito.when(sccpLongMessageRulesRepository.save(any())).thenReturn(sccpLongMessageRules);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.updateSccpLongMessageRules(sccpLongMessageRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.OK);
        ApiResponse apiResponse = response.getBody();
        var longMessageRule = (SccpLongMessageRulesDTO) Objects.requireNonNull(apiResponse).data();
        assertEquals("LUDT_ENABLED", longMessageRule.getLongMessageRuleType());

        Mockito.when(sccpLongMessageRulesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.updateSccpLongMessageRules(sccpLongMessageRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpLongMessageRulesRepository.findById(anyInt())).thenReturn(currentSccpLongMessageRules);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpLongMessageRulesRepository).save(any());
        response = ss7GatewaysController.updateSccpLongMessageRules(sccpLongMessageRulesDTOUpdated, 1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR"})
    @Test
    void deleteSccpLongMessageRulesTest() {
        SccpLongMessageRulesDTO sccpLongMessageRulesDTO = Utils.getSccpLongMessageRulesDTOMock();
        SccpLongMessageRules currentSccpLongMessageRules = sccpMapper.toEntityLongMessageRules(sccpLongMessageRulesDTO);

        Mockito.when(sccpLongMessageRulesRepository.findById(anyInt())).thenReturn(currentSccpLongMessageRules);
        Mockito.doNothing().when(sccpLongMessageRulesRepository).delete(currentSccpLongMessageRules);
        ResponseEntity<ApiResponse> response = ss7GatewaysController.deleteSccpLongMessageRules(1);
        checkAssertions(response, HttpStatus.OK,"DELETE");

        Mockito.when(sccpLongMessageRulesRepository.findById(anyInt())).thenReturn(null);
        response = ss7GatewaysController.deleteSccpLongMessageRules(1);
        checkAssertions(response, HttpStatus.NOT_FOUND);

        Mockito.when(sccpLongMessageRulesRepository.findById(anyInt())).thenReturn(currentSccpLongMessageRules);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(sccpLongMessageRulesRepository).delete(any());
        response = ss7GatewaysController.deleteSccpLongMessageRules(1);
        checkAssertions(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
