package com.smsc.management.app.catalog.service;

import com.smsc.management.app.catalog.model.repository.BalanceTypeRepository;
import com.smsc.management.app.catalog.model.repository.BindStatusesRepository;
import com.smsc.management.app.catalog.model.repository.BindsTypesRepository;
import com.smsc.management.app.catalog.model.repository.DeliveryStatusRepository;
import com.smsc.management.app.catalog.model.repository.EncodingTypeRepository;
import com.smsc.management.app.catalog.model.repository.InterfaceVersionsRepository;
import com.smsc.management.app.catalog.model.repository.NpiCatalogRepository;
import com.smsc.management.app.catalog.model.repository.TonCatalogRepository;
import com.smsc.management.app.diameter.model.repository.DiameterApplicationRepository;
import com.smsc.management.app.ss7.model.repository.FunctionalityRepository;
import com.smsc.management.app.ss7.model.repository.GlobalTitleIndicatorRepository;
import com.smsc.management.app.ss7.model.repository.LoadSharingAlgorithmRepository;
import com.smsc.management.app.ss7.model.repository.NatureOfAddressRepository;
import com.smsc.management.app.ss7.model.repository.NumberingPlanRepository;
import com.smsc.management.app.ss7.model.repository.OriginationTypeRepository;
import com.smsc.management.app.ss7.model.repository.RuleTypeRepository;
import com.smsc.management.app.ss7.model.repository.SlsRangeRepository;
import com.smsc.management.app.ss7.model.repository.TrafficModeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
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
    BindsTypesRepository bindTypeRepo;
    @Mock
    FunctionalityRepository functionalityRepo;
    @Mock
    TrafficModeRepository trafficModeRepo;
    @Mock
    OriginationTypeRepository originTypeRepo;
    @Mock
    RuleTypeRepository ruleTypeRepo;
    @Mock
    LoadSharingAlgorithmRepository loadSharingAlgorithmRepo;
    @Mock
    SlsRangeRepository slsRangeRepo;
    @Mock
    EncodingTypeRepository encodingTypeRepo;
    @Mock
    GlobalTitleIndicatorRepository gtIndicatorRepo;
    @Mock
    NumberingPlanRepository numberingPlanRepo;
    @Mock
    NatureOfAddressRepository natureOfAddressRepo;
    @Mock
    DiameterApplicationRepository appRepository;

    @InjectMocks
    CatalogService catalogService;

    @Test
    void getCatalog() {
        when(bindStatusesRepository.findAll()).thenThrow(new RuntimeException("Test exception"));
        assertDoesNotThrow(() -> catalogService.getCatalog("bindstatuses"));
    }
}