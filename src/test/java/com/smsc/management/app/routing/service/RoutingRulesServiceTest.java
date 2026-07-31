package com.smsc.management.app.routing.service;

import com.smsc.management.app.routing.dto.RedisRoutingRulesDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDestinationDTO;
import com.smsc.management.app.routing.mapper.RoutingRulesMapper;
import com.smsc.management.app.routing.model.entity.RoutingRules;
import com.smsc.management.app.routing.model.repository.CustomParamMatcherRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesActionAdvancedRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesDestinationRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingRulesServiceTest {

    @Mock
    RoutingRulesRepository routingRuleRepo;

    @Mock
    RoutingRulesDestinationRepository routingRulesDestinationRepo;

    @Mock
    SequenceNetworksIdRepository sequenceNetworkRepo;

    @Mock
    CustomParamMatcherRepository customParamMatcherRepo;

    @Mock
    RoutingRulesActionAdvancedRepository rulesActionAdvancedRepo;

    @Mock
    RoutingRulesMapper routingRulesMapper;

    @Mock
    UtilsBase utilsBase;

    @Mock
    AppProperties appProperties;

    @InjectMocks
    RoutingRulesService routingRulesService;

    @Test
    void getRoutingRulesNotEmptyListTest() {
        when(routingRuleRepo.findByRoutingRulesList()).thenReturn(List.of(new RoutingRulesDTO()));
        when(routingRulesDestinationRepo.findByRoutingRulesIdList(anyInt())).thenReturn(List.of(new RoutingRulesDestinationDTO()));
        assertEquals(200, routingRulesService.getRoutingRules().status());
    }

    @Test
    void getRoutingRulesAnyExceptionTest() {
        when(routingRuleRepo.findByRoutingRulesList()).thenThrow(new RuntimeException("RuntimeException"));
        assertEquals(500, routingRulesService.getRoutingRules().status());
    }

    @Test
    void createInvalidStructureExceptionTest() {
        RoutingRulesDTO routingRulesDTO = new RoutingRulesDTO();
        List<RoutingRulesDestinationDTO> destinationDTOS = new ArrayList<>();
        routingRulesDTO.setDestination(destinationDTOS);
        assertEquals(400, routingRulesService.create(routingRulesDTO).status());
    }

    @Test
    void createDataIntegrityExceptionTest() {
        RoutingRulesDTO routingRulesDTO = getRoutingRuleDtoMock(1);
        when(routingRuleRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        assertThrows(DataIntegrityViolationException.class, () -> routingRulesService.create(routingRulesDTO));
    }

    @Test
    void createDataIntegrityExceptionWithRollbackTest() {
        RoutingRulesDTO routingRulesDTO = getRoutingRuleDtoMock(1);
        RoutingRules routingRules = getRoutingRuleMock();
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(routingRuleRepo.save(any())).thenReturn(routingRules);
        when(routingRulesDestinationRepo.saveAll(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        assertThrows(DataIntegrityViolationException.class, () -> routingRulesService.create(routingRulesDTO));
    }

    @Test
    void createAnyExceptionTest() {
        ApiResponse response = routingRulesService.create(null);
        assertEquals(500, response.status());
    }

    @Test
    void createSocketWithActionFalse() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(routingRuleRepo.save(any())).thenReturn(new RoutingRules());
        when(sequenceNetworkRepo.findById(1)).thenReturn(sequenceNetworksId);
        when(routingRuleRepo.findOriginProtocol(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.create(routingRulesDto);
        assertEquals(200, response.status());
    }

    @Test
    void updateWithNoDestinationTest() {
        RoutingRulesDTO routingRulesDto = new RoutingRulesDTO();
        List<RoutingRulesDestinationDTO> destinationDTOS = new ArrayList<>();
        routingRulesDto.setDestination(destinationDTOS);
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(400, response.status());
    }

    @Test
    void updateWithNoRoutingRulesTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(404, response.status());
    }

    @Test
    void updateNoRoutingFirstActionTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        when(routingRuleRepo.findById(anyInt())).thenReturn(new RoutingRules());
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(200, response.status());
    }

    @Test
    void updateNoRoutingSecondActionTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(2);
        when(routingRuleRepo.findById(anyInt())).thenReturn(new RoutingRules());
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(200, response.status());
    }

    @Test
    void updateNoRoutingAnyActionTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(-1);
        when(routingRuleRepo.findById(anyInt())).thenReturn(new RoutingRules());
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(400, response.status());
    }

    @Test
    void updateDataIntegrityExceptionTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        when(routingRuleRepo.findById(anyInt())).thenReturn(new RoutingRules());
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        when(routingRuleRepo.save(any())).thenThrow(new DataIntegrityViolationException("DataIntegrityViolationException"));
        assertThrows(DataIntegrityViolationException.class, () -> routingRulesService.update(-1, routingRulesDto));
    }

    @Test
    void updateAnyExceptionTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        when(routingRuleRepo.findById(anyInt())).thenReturn(new RoutingRules());
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        when(routingRuleRepo.save(any())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(500, response.status());
    }


    @Test
    void updateWithRedisNetworkIdTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        RoutingRules routingRules = getRoutingRuleMock();
        when(routingRuleRepo.findById(anyInt())).thenReturn(routingRules);
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(200, response.status());
    }

    @Test
    void updateWithRedisNetworkIdEmptyValueTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        RoutingRules routingRules = getRoutingRuleMock();
        when(routingRuleRepo.findById(anyInt())).thenReturn(routingRules);
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        when(routingRuleRepo.findOriginProtocol(anyInt()))
                .thenReturn("")
                .thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(200, response.status());
    }

    @Test
    void updateWithRedisNetworkIdAnyExceptionTest() {
        RoutingRulesDTO routingRulesDto = getRoutingRuleDtoMock(1);
        RoutingRules routingRules = getRoutingRuleMock();
        when(routingRuleRepo.findById(anyInt())).thenReturn(routingRules);
        SequenceNetworksId sequenceNetworksId = getSequenceNetworkIdMock();
        when(sequenceNetworkRepo.findById(anyInt())).thenReturn(sequenceNetworksId);
        when(routingRuleRepo.findOriginProtocol(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.update(-1, routingRulesDto);
        assertEquals(200, response.status());
    }

    @Test
    void deleteNoRoutingRuleFoundTest() {
        when(routingRuleRepo.findById(anyInt())).thenReturn(null);
        ApiResponse response = routingRulesService.delete(anyInt());
        assertEquals(404, response.status());
    }

    @Test
    void deleteWithEmptyRoutingRuleTest() {
        when(routingRuleRepo.findById(anyInt())).thenReturn(new RoutingRules());
        when(routingRuleRepo.findOriginProtocol(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.delete(anyInt());
        assertEquals(200, response.status());
    }

    @Test
    void deleteAnyExceptionTest() {
        when(routingRuleRepo.findById(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.delete(anyInt());
        assertEquals(500, response.status());
    }

    @Test
    void socketAndRedisActionRoutingNotEmptyTest() {
        when(routingRuleRepo.findOriginProtocol(anyInt())).thenReturn("SMPP");
        when(utilsBase.getRoutingRules(anyInt(), anyString())).thenReturn(List.of(new RedisRoutingRulesDTO()));
        assertTrue(routingRulesService.socketAndRedisAction(2));
    }

    @Test
    void manualRollbackAnyExceptionTest() {
        when(routingRulesDestinationRepo.findByRoutingRulesId(anyInt())).thenThrow(new RuntimeException("RuntimeException"));
        assertDoesNotThrow(() -> routingRulesService.manualRollback(anyInt()));
    }

    @Test
    void getNetworksAnyExceptionTest() {
        when(routingRuleRepo.findGatewayNamesAndIds()).thenThrow(new RuntimeException("RuntimeException"));
        ApiResponse response = routingRulesService.getNetworks();
        assertEquals(500, response.status());
    }

    private RoutingRulesDTO getRoutingRuleDtoMock(int action) {
        RoutingRulesDTO routingRulesDTO = new RoutingRulesDTO();
        List<RoutingRulesDestinationDTO> destinationDTOS = new ArrayList<>();
        RoutingRulesDestinationDTO routingRulesDestinationDTO = new RoutingRulesDestinationDTO();
        routingRulesDestinationDTO.setRoutingRulesId(5);
        routingRulesDestinationDTO.setName("Origin");
        routingRulesDestinationDTO.setPriority(1);
        routingRulesDestinationDTO.setAction(action);
        routingRulesDestinationDTO.setNetworkType("Type");
        routingRulesDestinationDTO.setNetworkId(1);
        destinationDTOS.add(routingRulesDestinationDTO);
        routingRulesDTO.setDestination(destinationDTOS);
        return routingRulesDTO;
    }

    private RoutingRules getRoutingRuleMock() {
        RoutingRules routingRules = new RoutingRules();
        routingRules.setId(1);
        routingRules.setOriginNetworkId(1);
        return routingRules;
    }

    private SequenceNetworksId getSequenceNetworkIdMock() {
        SequenceNetworksId sequenceNetworksId = new SequenceNetworksId();
        sequenceNetworksId.setNetworkType("NET");
        return sequenceNetworksId;
    }
}
