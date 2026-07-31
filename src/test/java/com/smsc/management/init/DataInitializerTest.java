package com.smsc.management.init;

import com.paicbd.smsc.dto.SmppServerConfig;
import com.paicbd.smsc.scylla.ScyllaManager;
import com.smsc.management.app.catalog.model.repository.BalanceTypeRepository;
import com.smsc.management.app.catalog.model.repository.BindStatusesRepository;
import com.smsc.management.app.catalog.model.repository.BindsTypesRepository;
import com.smsc.management.app.catalog.model.repository.DeliveryStatusRepository;
import com.smsc.management.app.catalog.model.repository.EncodingTypeRepository;
import com.smsc.management.app.catalog.model.repository.InterfaceVersionsRepository;
import com.smsc.management.app.catalog.model.repository.NpiCatalogRepository;
import com.smsc.management.app.catalog.model.repository.TonCatalogRepository;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.server.mapper.ServerMapper;
import com.smsc.management.app.server.model.entity.SmppServer;
import com.smsc.management.app.server.model.repository.SmppServerRepository;
import com.smsc.management.app.settings.dto.GeneralSettingsSmppHttpDTO;
import com.smsc.management.app.settings.dto.GeneralSmscRetryDTO;
import com.smsc.management.app.settings.mapper.GeneralSettingsMapper;
import com.smsc.management.app.settings.model.entity.CommonVariables;
import com.smsc.management.app.settings.model.entity.GeneralSettingsSmppHttp;
import com.smsc.management.app.settings.model.entity.GeneralSmscRetry;
import com.smsc.management.app.settings.model.repository.CommonVariablesRepository;
import com.smsc.management.app.settings.model.repository.GeneralSettingsSmppHttpRepository;
import com.smsc.management.app.settings.model.repository.GeneralSmscRetryRepository;
import com.smsc.management.app.settings.service.CommonVariableService;
import com.smsc.management.app.ss7.model.repository.FunctionalityRepository;
import com.smsc.management.app.ss7.model.repository.GlobalTitleIndicatorRepository;
import com.smsc.management.app.ss7.model.repository.LoadSharingAlgorithmRepository;
import com.smsc.management.app.ss7.model.repository.NatureOfAddressRepository;
import com.smsc.management.app.ss7.model.repository.NumberingPlanRepository;
import com.smsc.management.app.ss7.model.repository.OriginationTypeRepository;
import com.smsc.management.app.ss7.model.repository.RuleTypeRepository;
import com.smsc.management.app.ss7.model.repository.SlsRangeRepository;
import com.smsc.management.app.ss7.model.repository.TrafficModeRepository;
import com.smsc.management.app.user.model.entity.UserServiceProvider;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.app.user.service.UserService;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    UserService userService;
    @Mock
    InterfaceVersionsRepository interfaceVersionsRepository;
    @Mock
    BindStatusesRepository bindStatusesRepository;
    @Mock
    NpiCatalogRepository npiRepo;
    @Mock
    TonCatalogRepository tonRepo;
    @Mock
    BalanceTypeRepository balanceTypeRepo;
    @Mock
    DeliveryStatusRepository deliveryStatusRepo;
    @Mock
    BindsTypesRepository bindsTypesRepo;
    @Mock
    FunctionalityRepository functionalityRepo;
    @Mock
    SlsRangeRepository slsRangeRepo;
    @Mock
    TrafficModeRepository trafficModeRepo;
    @Mock
    RuleTypeRepository sccpRuleTypeRepo;
    @Mock
    OriginationTypeRepository originationTypeRepo;
    @Mock
    LoadSharingAlgorithmRepository loadSharingAlgorithmRepo;
    @Mock
    NumberingPlanRepository numberingPlanRepo;
    @Mock
    NatureOfAddressRepository natureOfAddressRepo;
    @Mock
    EncodingTypeRepository encodingTypeRepo;
    @Mock
    GeneralSettingsSmppHttpRepository generalSettingsHttpSmppRepo;
    @Mock
    GeneralSmscRetryRepository generalSmscRetryRepo;
    @Mock
    GlobalTitleIndicatorRepository gtIndicatorRepo;
    @Mock
    CommonVariablesRepository commonVariablesRepo;
    @Mock
    SmppServerRepository smppServerRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    UserServiceProviderRepository userServiceProviderRepository;
    @Mock
    ServiceProviderRepository serviceProviderRepository;
    @Mock
    GeneralSettingsMapper generalSettingsMapper;
    @Mock
    ServerMapper serverMapper;
    @Mock
    ScyllaManager scyllaManager;
    @Mock
    UtilsBase utilsBase;
    @Mock
    CommonVariableService commonVariableService;

    @InjectMocks
    DataInitializer dataInitializer;

    @Test
    void initShouldCreateDefaultsAndUpdateRedis() {
        GeneralSettingsSmppHttp httpEntity = new GeneralSettingsSmppHttp();
        GeneralSmscRetry retryEntity = new GeneralSmscRetry();
        SmppServerConfig smppServerConfig = Mockito.mock(SmppServerConfig.class);
        SmppServer forcedStopped = new SmppServer();
        forcedStopped.setId(200);
        forcedStopped.setStatus(Constants.FORCED_STOPPED_STATUS);
        SmppServer stopped = new SmppServer();
        stopped.setId(201);
        stopped.setStatus(Constants.DEFAULT_STATUS);

        when(userServiceProviderRepository.count()).thenReturn(1L);
        when(scyllaManager.keyspaceExists()).thenReturn(true);
        when(generalSettingsHttpSmppRepo.findById(1)).thenReturn(null);
        when(generalSettingsMapper.toEntity(any(GeneralSettingsSmppHttpDTO.class))).thenReturn(httpEntity);
        when(generalSettingsMapper.toDTO(httpEntity)).thenReturn(new GeneralSettingsSmppHttpDTO());
        when(generalSmscRetryRepo.findById(1)).thenReturn(null);
        when(generalSettingsMapper.toEntitySmscRetry(any(GeneralSmscRetryDTO.class))).thenReturn(retryEntity);
        when(generalSettingsMapper.toDTOSmscRetry(retryEntity)).thenReturn(new GeneralSmscRetryDTO());
        when(commonVariablesRepo.findByKey(anyString())).thenReturn(null, null, null, null);
        when(smppServerRepository.existsByIsDefaultTrue()).thenReturn(false);
        when(smppServerRepository.findAll()).thenReturn(List.of(forcedStopped, stopped));
        when(smppServerRepository.save(any(SmppServer.class))).thenAnswer(invocation -> {
            SmppServer server = invocation.getArgument(0);
            if (server.getId() == 0) {
                server.setId(100);
            }
            return server;
        });
        when(serverMapper.toRedisDTOFromEntity(any(SmppServer.class))).thenReturn(smppServerConfig);

        dataInitializer.init();

        verify(userService).createRootUser();
        verify(scyllaManager).createDndEntriesTable();
        verify(generalSettingsHttpSmppRepo).save(httpEntity);
        verify(generalSmscRetryRepo).save(retryEntity);
        verify(commonVariablesRepo, times(4)).save(any(CommonVariables.class));
        verify(commonVariableService).syncRedisReplicatedVariables();
        verify(smppServerRepository, times(3)).save(any(SmppServer.class));
        verify(utilsBase, times(5)).sendNotificationSocket(anyString(), anyString());

        ArgumentCaptor<SmppServer> captor = ArgumentCaptor.forClass(SmppServer.class);
        verify(smppServerRepository, times(3)).save(captor.capture());
        List<SmppServer> savedServers = captor.getAllValues();
        assertTrue(savedServers.stream().anyMatch(server -> Constants.DEFAULT_SMPP_SERVER_NAME.equals(server.getName())));
        assertTrue(savedServers.stream().anyMatch(server -> Constants.STARTED_STATUS.equals(server.getStatus())));
    }

    @Test
    void initShouldSkipCreateDndTableAndReuseExistingSettings() {
        GeneralSettingsSmppHttp httpEntity = new GeneralSettingsSmppHttp();
        GeneralSmscRetry retryEntity = new GeneralSmscRetry();
        SmppServerConfig smppServerConfig = Mockito.mock(SmppServerConfig.class);
        SmppServer server = new SmppServer();
        server.setId(300);
        server.setStatus(Constants.DEFAULT_STATUS);

        when(userServiceProviderRepository.count()).thenReturn(1L);
        when(scyllaManager.keyspaceExists()).thenReturn(false);
        when(generalSettingsHttpSmppRepo.findById(1)).thenReturn(httpEntity);
        when(generalSettingsMapper.toDTO(httpEntity)).thenReturn(new GeneralSettingsSmppHttpDTO());
        when(generalSmscRetryRepo.findById(1)).thenReturn(retryEntity);
        when(generalSettingsMapper.toDTOSmscRetry(retryEntity)).thenReturn(new GeneralSmscRetryDTO());
        when(commonVariablesRepo.findByKey(anyString())).thenReturn(new CommonVariables(), new CommonVariables(), new CommonVariables(), new CommonVariables());
        when(smppServerRepository.existsByIsDefaultTrue()).thenReturn(true);
        when(smppServerRepository.findAll()).thenReturn(List.of(server));
        when(smppServerRepository.save(any(SmppServer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(serverMapper.toRedisDTOFromEntity(any(SmppServer.class))).thenReturn(smppServerConfig);

        dataInitializer.init();

        verify(scyllaManager, never()).createDndEntriesTable();
        verify(generalSettingsHttpSmppRepo, never()).save(any(GeneralSettingsSmppHttp.class));
        verify(generalSmscRetryRepo, never()).save(any(GeneralSmscRetry.class));
        verify(commonVariablesRepo, never()).save(any(CommonVariables.class));
        verify(smppServerRepository, times(1)).save(any(SmppServer.class));
        assertNotEquals(Constants.FORCED_STOPPED_STATUS, server.getStatus());
    }

    @Test
    void initUserProvidersShouldCreateAssignmentsUsingFindUsersAll() throws Exception {
        Users user = new Users();
        ServiceProvider httpProvider = new ServiceProvider();
        httpProvider.setProtocol("HTTP");
        ServiceProvider smppProvider = new ServiceProvider();
        smppProvider.setProtocol("SMPP");

        when(userServiceProviderRepository.count()).thenReturn(0L);
        when(userRepository.findUsersAll()).thenReturn(List.of(user));
        when(serviceProviderRepository.findByEnabledNot(2)).thenReturn(List.of(httpProvider, smppProvider));

        Method method = DataInitializer.class.getDeclaredMethod("initUserProviders");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(dataInitializer));

        ArgumentCaptor<List<UserServiceProvider>> captor = ArgumentCaptor.forClass(List.class);
        verify(userServiceProviderRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void initUserProvidersShouldFallbackToFindAllWhenFindUsersAllEmpty() throws Exception {
        Users user = new Users();
        ServiceProvider httpProvider = new ServiceProvider();
        httpProvider.setProtocol("HTTP");

        when(userServiceProviderRepository.count()).thenReturn(0L);
        when(userRepository.findUsersAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(serviceProviderRepository.findByEnabledNot(2)).thenReturn(List.of(httpProvider));

        Method method = DataInitializer.class.getDeclaredMethod("initUserProviders");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(dataInitializer));

        verify(userRepository).findAll();
        verify(userServiceProviderRepository).saveAll(any());
    }

    @Test
    void initUserProvidersShouldSkipWhenNoUsersOrProviders() throws Exception {
        when(userServiceProviderRepository.count()).thenReturn(0L);
        when(userRepository.findUsersAll()).thenReturn(null);
        when(userRepository.findAll()).thenReturn(List.of());
        when(serviceProviderRepository.findByEnabledNot(2)).thenReturn(List.of());

        Method method = DataInitializer.class.getDeclaredMethod("initUserProviders");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(dataInitializer));

        verify(userServiceProviderRepository, never()).saveAll(any());
    }

    @Test
    void initUserProvidersShouldSkipWhenUsersExistButNoHttpProviders() throws Exception {
        Users user = new Users();
        ServiceProvider smppProvider = new ServiceProvider();
        smppProvider.setProtocol("SMPP");

        when(userServiceProviderRepository.count()).thenReturn(0L);
        when(userRepository.findUsersAll()).thenReturn(List.of(user));
        when(serviceProviderRepository.findByEnabledNot(2)).thenReturn(List.of(smppProvider));

        Method method = DataInitializer.class.getDeclaredMethod("initUserProviders");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(dataInitializer));

        verify(userServiceProviderRepository, never()).saveAll(any());
    }
}
