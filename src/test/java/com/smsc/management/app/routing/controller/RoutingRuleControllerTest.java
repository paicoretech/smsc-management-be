package com.smsc.management.app.routing.controller;

import com.smsc.management.app.routing.dto.CustomParamMatcherDTO;
import com.smsc.management.app.routing.dto.RoutingRulesActionAdvancedDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDestinationDTO;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.integration.BaseIntegrationTest;
import com.smsc.management.utils.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

class RoutingRuleControllerTest extends BaseIntegrationTest {

    @Autowired
    private RoutingRuleController routingRuleController;

    @Autowired
    private SequenceNetworksIdRepository sequenceNetworksIdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceProviderRepository userServiceProviderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("unchecked")
    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR", "ACTIONS_ADVANCED"})
    @Test
    @DisplayName("Create new routing rule and get data when data is ok then successfully")
    void createAndGetAndUpdateRedisWhenDataIsOkThenSuccessfully() {
        RoutingRulesDTO routingRulesDTO = createAndGetMockRoutingRuleDto(false);
        RoutingRulesActionAdvancedDTO actionAdvancedDTO = createAndGetMockRoutingRuleActionAdvancedDto();
        routingRulesDTO.setActionAdvanced(actionAdvancedDTO);
        ApiResponse response = routingRuleController.create(routingRulesDTO).getBody();
        assertNotNull(response);
        assertEquals(200, response.status());

        response = routingRuleController.getListRouting().getBody();
        assertNotNull(response);
        assertEquals(200, response.status());
        List<RoutingRulesDTO> routingResponse = (List<RoutingRulesDTO>) response.data();
        assertNotNull(routingResponse);
        assertEquals(1, routingResponse.size());
        assertInstanceOf(ApiResponse.class, response);
        assertInstanceOf(List.class, response.data());

        response = routingRuleController.updateRedis(routingResponse.getFirst().getOriginNetworkId()).getBody();
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR", "ACTIONS_ADVANCED"})
    @Test
    void getNetworksTest() {
        setupMockAdminUser();
        ApiResponse response = routingRuleController.getNetworks().getBody();
        assertNotNull(response);
        assertEquals(200, response.status());
        assertInstanceOf(ApiResponse.class, response);
        assertInstanceOf(List.class, response.data());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR", "ACTIONS_ADVANCED"})
    @ParameterizedTest
    @CsvSource({
            "45, 44",
            "22, 46",
            "-1, 10",
            "10, -1"
    })
    void createRoutingRuleTest(int opcSRI, int opcMT) {
        RoutingRulesDTO routingRulesDTO = createAndGetMockRoutingRuleDto(false);
        RoutingRulesActionAdvancedDTO actionAdvancedDTO = createAndGetMockRoutingRuleActionAdvancedDto();
        actionAdvancedDTO.setOperationCodeSri(opcSRI);
        actionAdvancedDTO.setOperationCodeMt(opcMT);
        routingRulesDTO.setActionAdvanced(actionAdvancedDTO);

        ApiResponse response = routingRuleController.create(routingRulesDTO).getBody();
        assertNotNull(response);
        if (opcMT == 10 || opcSRI == 10) {
            assertEquals(500, response.status());
        } else {
            assertEquals(200, response.status());
        }
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR", "ACTIONS_ADVANCED"})
    @Test
    void createRoutingRuleWhenCustomParamMatchersIsDuplicatedThenHTTPStatusCodeIs400() {
        RoutingRulesDTO routingRulesDTO = createAndGetMockRoutingRuleDto(true);
        routingRulesDTO.setActionAdvanced(createAndGetMockRoutingRuleActionAdvancedDto());
        ApiResponse response = routingRuleController.create(routingRulesDTO).getBody();
        assertNotNull(response);
        assertEquals(400, response.status());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR", "ACTIONS_ADVANCED"})
    @Test
    void updateRoutingRuleTest() {
        RoutingRulesDTO routingRulesDTO = createAndGetMockRoutingRuleDto(false);
        routingRulesDTO.setActionAdvanced(createAndGetMockRoutingRuleActionAdvancedDto());
        routingRuleController.create(routingRulesDTO);
        routingRulesDTO.setDropTempFailure(false);
        ApiResponse response = routingRuleController.update(routingRulesDTO, routingRulesDTO.getId()).getBody();
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @WithMockUser(roles = {"ROOT", "ADMINISTRATOR", "ACTIONS_ADVANCED"})
    @Test
    void deleteRoutingRuleTest() {
        RoutingRulesDTO routingRulesDTO = createAndGetMockRoutingRuleDto(false);
        routingRulesDTO.setActionAdvanced(createAndGetMockRoutingRuleActionAdvancedDto());
        RoutingRulesDTO newRouting = (RoutingRulesDTO) Objects.requireNonNull(routingRuleController.create(routingRulesDTO).getBody()).data();
        ApiResponse response = routingRuleController.delete(newRouting.getId()).getBody();
        assertNotNull(response);
        assertEquals(200, response.status());
    }

    private RoutingRulesDTO createAndGetMockRoutingRuleDto(boolean customParamMatcherError) {
        RoutingRulesDTO routingRulesDTO = new RoutingRulesDTO();
        List<RoutingRulesDestinationDTO> destinationsMock = createAndGetMockRoutingRuleDestinationDto();
        routingRulesDTO.setOriginNetworkId(destinationsMock.getFirst().getNetworkId());
        routingRulesDTO.setDestination(createAndGetMockRoutingRuleDestinationDto());
        routingRulesDTO.setCustomParamMatcher(this.createAndGetMockCustomParamMatcherDto(customParamMatcherError));
        return routingRulesDTO;
    }

    private List<RoutingRulesDestinationDTO> createAndGetMockRoutingRuleDestinationDto() {
        List<RoutingRulesDestinationDTO> destinationDTOS = new ArrayList<>();
        RoutingRulesDestinationDTO routingRulesDestinationDTO = new RoutingRulesDestinationDTO();
        SequenceNetworksId sequenceNetworksId = createAndGetMockSequenceNetwork();
        routingRulesDestinationDTO.setRoutingRulesId(5);
        routingRulesDestinationDTO.setName("Origin");
        routingRulesDestinationDTO.setPriority(1);
        routingRulesDestinationDTO.setAction(1);
        routingRulesDestinationDTO.setNetworkId(sequenceNetworksId.getId());
        routingRulesDestinationDTO.setNetworkType("Type");
        destinationDTOS.add(routingRulesDestinationDTO);
        return destinationDTOS;
    }

    private List<CustomParamMatcherDTO> createAndGetMockCustomParamMatcherDto(boolean withError) {
        List<CustomParamMatcherDTO> customParamMatcherDTOS = new ArrayList<>();

        CustomParamMatcherDTO propertyString = new CustomParamMatcherDTO();
        propertyString.setPropertyName("propertyString");
        propertyString.setValueMatcher("test");

        CustomParamMatcherDTO propertyInt = new CustomParamMatcherDTO();
        propertyInt.setPropertyName("propertyInt");
        propertyInt.setValueMatcher("12345");

        CustomParamMatcherDTO propertyBoolean = new CustomParamMatcherDTO();
        propertyBoolean.setPropertyName("propertyBoolean");
        propertyBoolean.setValueMatcher("true");

        if (withError) {
            CustomParamMatcherDTO propertyDuplicated = new CustomParamMatcherDTO();
            propertyDuplicated.setPropertyName("propertyBoolean");
            propertyDuplicated.setValueMatcher("true");
            customParamMatcherDTOS.add(propertyDuplicated);
        }

        customParamMatcherDTOS.add(propertyString);
        customParamMatcherDTOS.add(propertyInt);
        customParamMatcherDTOS.add(propertyBoolean);

        return customParamMatcherDTOS;
    }

    private SequenceNetworksId createAndGetMockSequenceNetwork() {
        SequenceNetworksId sequenceNetworksId = new SequenceNetworksId();
        sequenceNetworksId.setNetworkType("NET");
        return sequenceNetworksIdRepository.save(sequenceNetworksId);
    }

    private RoutingRulesActionAdvancedDTO createAndGetMockRoutingRuleActionAdvancedDto() {
        RoutingRulesActionAdvancedDTO routingRulesActionAdvancedDTO = new RoutingRulesActionAdvancedDTO();
        routingRulesActionAdvancedDTO.setMapVersion(2);
        routingRulesActionAdvancedDTO.setSsnHlrSri(6);
        routingRulesActionAdvancedDTO.setSsnSmscMt(8);
        routingRulesActionAdvancedDTO.setSsnMscMt(8);
        routingRulesActionAdvancedDTO.setSsnMscMt(8);
        routingRulesActionAdvancedDTO.setSsnSmscSri(8);
        routingRulesActionAdvancedDTO.setSccpSourceAddressSri("1234567890");
        routingRulesActionAdvancedDTO.setSccpSourceAddressMt("9876543210");
        routingRulesActionAdvancedDTO.setSccpDestinationAddressMt("5432109876");
        routingRulesActionAdvancedDTO.setSccpDestinationAddressSri("987654321");
        return routingRulesActionAdvancedDTO;
    }

    private void setupMockAdminUser() {
        Users mockUser = new Users();
        mockUser.setId(1);
        mockUser.setUserName("user");
        mockUser.setPassword("password");
        mockUser.setRoles(List.of("ROOT"));
        mockUser.setAllServiceProviders(true);
        userRepository.save(mockUser);
    }
}
