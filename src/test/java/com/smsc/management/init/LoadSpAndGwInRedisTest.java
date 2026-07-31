package com.smsc.management.init;

import com.smsc.management.app.errorcode.service.ErrorCodeMappingService;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.gateway.service.GatewaysService;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.provider.service.ServiceProviderService;
import com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.routing.service.RoutingRulesService;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.sip.service.ObjectSipService;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
import com.smsc.management.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadSpAndGwInRedisTest {
    GatewaysRepository gatewaysRepository;
    ServiceProviderRepository spRepo;
    Ss7GatewaysRepository ss7GwRepo;
    SipGatewaysRepository sipGwRepo;
    OperatorMnoRepository opMnoRepo;
    RoutingRulesRepository routingRulesRepo;

    ServiceProviderService processSp;
    GatewaysService processGw;
    ObjectSs7Service processObjSs7;
    ObjectSipService processObjSip;
    ErrorCodeMappingService processError;
    RoutingRulesService processRoutingRules;

    @BeforeEach
    void setUp() {
        processGw = Mockito.mock(GatewaysService.class);
        gatewaysRepository = Mockito.mock(GatewaysRepository.class);
        processSp = Mockito.mock(ServiceProviderService.class);
        spRepo = Mockito.mock(ServiceProviderRepository.class);
        processObjSs7 = Mockito.mock(ObjectSs7Service.class);
        ss7GwRepo = Mockito.mock(Ss7GatewaysRepository.class);
        processObjSip = Mockito.mock(ObjectSipService.class);
        sipGwRepo = Mockito.mock(SipGatewaysRepository.class);
        processRoutingRules = Mockito.mock(RoutingRulesService.class);
        routingRulesRepo = Mockito.mock(RoutingRulesRepository.class);
        opMnoRepo = Mockito.mock(OperatorMnoRepository.class);
        processError = Mockito.mock(ErrorCodeMappingService.class);
    }

    @Test
    void init() throws NoSuchMethodException {
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                gatewaysRepository, spRepo, ss7GwRepo, sipGwRepo, opMnoRepo, routingRulesRepo,
                processSp, processGw, processObjSs7, processObjSip, processError, processRoutingRules);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("init");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void nullPointerExceptions() throws NoSuchMethodException {
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, null, null, null,
                null, null, null, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("init");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadGw() throws NoSuchMethodException {
        Gateways gateways = Mockito.mock(Gateways.class);
        when(gatewaysRepository.findByEnabledNot(Constants.DELETED_ENABLED_STATUS)).thenReturn(List.of(gateways));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                gatewaysRepository, null, null, null, null, null,
                null, processGw, null, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadGw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadGwListIsNull() throws NoSuchMethodException {
        when(gatewaysRepository.findByEnabledNot(Constants.DELETED_ENABLED_STATUS)).thenReturn(null);
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                gatewaysRepository, null, null, null, null, null,
                null, processGw, null, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadGw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSp() throws NoSuchMethodException {
        ServiceProvider serviceProvider = Mockito.mock(ServiceProvider.class);
        when(spRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS)).thenReturn(List.of(serviceProvider));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, spRepo, null, null, null, null,
                processSp, null, null, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSp");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSpListIsNull() throws NoSuchMethodException {
        when(spRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS)).thenReturn(null);
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, spRepo, null, null, null, null,
                processSp, null, null, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSp");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSs7Gw() throws NoSuchMethodException {
        Ss7Gateways ss7Gateways = Mockito.mock(Ss7Gateways.class);
        when(ss7GwRepo.findByEnabledNotAndAllowedTraffic(Constants.DELETED_ENABLED_STATUS, Constants.SS7_ALLOWED_TRAFFIC)).thenReturn(List.of(ss7Gateways));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, ss7GwRepo, null, null, null,
                null, null, processObjSs7, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSs7Gw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSs7GwProcessIsNull() throws NoSuchMethodException {
        Ss7Gateways ss7Gateways = Mockito.mock(Ss7Gateways.class);
        when(ss7GwRepo.findByEnabledNotAndAllowedTraffic(Constants.DELETED_ENABLED_STATUS, Constants.SS7_ALLOWED_TRAFFIC)).thenReturn(List.of(ss7Gateways));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, ss7GwRepo, null, null, null,
                null, null, null, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSs7Gw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSs7GwListIsNull() throws NoSuchMethodException {
        when(ss7GwRepo.findByEnabledNotAndAllowedTraffic(Constants.DELETED_ENABLED_STATUS, Constants.SS7_ALLOWED_TRAFFIC)).thenReturn(null);
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, ss7GwRepo, null, null, null,
                null, null, processObjSs7, null, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSs7Gw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSipGw() throws NoSuchMethodException {
        SipGateways sipGateway = Mockito.mock(SipGateways.class);
        when(sipGwRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS)).thenReturn(List.of(sipGateway));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, sipGwRepo, null, null,
                null, null, null, processObjSip, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSipGw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadSipGwListIsNull() throws NoSuchMethodException {
        when(sipGwRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS)).thenReturn(null);
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, sipGwRepo, null, null,
                null, null, null, processObjSip, null, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadSipGw");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadErrorMapping() throws NoSuchMethodException {
        OperatorMno operatorMno = Mockito.mock(OperatorMno.class);
        when(opMnoRepo.findByEnabledTrue()).thenReturn(List.of(operatorMno));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, null, opMnoRepo, null,
                null, null, null, null, processError, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadErrorMapping");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadErrorMappingListIsNull() throws NoSuchMethodException {
        when(opMnoRepo.findByEnabledTrue()).thenReturn(null);
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, null, opMnoRepo, null,
                null, null, null, null, processError, null);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadErrorMapping");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadRoutingRules() throws NoSuchMethodException {
        NetworksToRoutingRulesDTO routingRule = Mockito.mock(NetworksToRoutingRulesDTO.class);
        when(routingRulesRepo.findGatewayNamesAndIds()).thenReturn(List.of(routingRule));
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, null, null, routingRulesRepo,
                null, null, null, null, null, processRoutingRules);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadRoutingRules");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }

    @Test
    void loadRoutingRulesListIsNull() throws NoSuchMethodException {
        when(routingRulesRepo.findGatewayNamesAndIds()).thenReturn(null);
        LoadSpAndGwInRedis loadSpAndGwInRedis = new LoadSpAndGwInRedis(
                null, null, null, null, null, routingRulesRepo,
                null, null, null, null, null, processRoutingRules);
        Method method = LoadSpAndGwInRedis.class.getDeclaredMethod("loadRoutingRules");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(loadSpAndGwInRedis));
    }
}
