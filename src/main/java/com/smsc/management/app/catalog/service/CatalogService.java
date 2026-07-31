package com.smsc.management.app.catalog.service;

import com.smsc.management.app.ss7.model.repository.SlsRangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.smsc.management.utils.ApiResponse;
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
import com.smsc.management.app.ss7.model.repository.OriginationTypeRepository;
import com.smsc.management.app.ss7.model.repository.RuleTypeRepository;
import com.smsc.management.app.ss7.model.repository.TrafficModeRepository;
import com.smsc.management.app.ss7.model.repository.NatureOfAddressRepository;
import com.smsc.management.app.ss7.model.repository.NumberingPlanRepository;
import com.smsc.management.utils.ResponseMapping;


/**
 * Service class for managing catalogs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {
	private final InterfaceVersionsRepository interfaceVersionsRepository;
	private final BindStatusesRepository bindStatusesRepository;
	private final NpiCatalogRepository npiRepo;
	private final TonCatalogRepository tonRepo;
	private final BalanceTypeRepository balanceTypeRepo;
	private final DeliveryStatusRepository deliveryStatusRepo;
	private final BindsTypesRepository bindTypeRepo;
	private final FunctionalityRepository functionalityRepo;
	private final TrafficModeRepository trafficModeRepo;
	private final OriginationTypeRepository originTypeRepo;
	private final RuleTypeRepository ruleTypeRepo;
	private final LoadSharingAlgorithmRepository loadSharingAlgorithmRepo;
	private final SlsRangeRepository slsRangeRepo;
	private final EncodingTypeRepository encodingTypeRepo;
	private final GlobalTitleIndicatorRepository gtIndicatorRepo;
	private final NumberingPlanRepository numberingPlanRepo;
	private final NatureOfAddressRepository natureOfAddressRepo;
	private final DiameterApplicationRepository appRepository;

	/**
     * Retrieves a catalog based on the specified catalog type.
     *
     * @param catalogType The type of catalog to retrieve.
     * @return A ResponseDTO containing the catalog data.
     */
	public ApiResponse getCatalog(String catalogType) {
		Object result;
		
		try {
			switch (catalogType) {
		        case "bindstatuses"  -> result = bindStatusesRepository.findAll();
		        case "balancetype"  -> result = balanceTypeRepo.findAll();
		        case "interfazversion"  -> result = interfaceVersionsRepository.findAll();
		        case "npicatalog"  -> result = npiRepo.findByIdNot(-1);
		        case "toncatalog"  -> result = tonRepo.findByIdNot(-1);
		        case "npicatalogrules"  -> result = npiRepo.findAll();
		        case "toncatalogrules"  -> result = tonRepo.findAll();
		        case "deliverystatus"  -> result = deliveryStatusRepo.findAll();
				case "bindtypes"  -> result = bindTypeRepo.findAll();
				case "ss7Functionality"  -> result = functionalityRepo.findAll();
				case "ss7TrafficMode"  -> result = trafficModeRepo.findAll();
				case "ss7OriginType"  -> result = originTypeRepo.findAll();
				case "ss7RuleType"  -> result = ruleTypeRepo.findAll();
				case "ss7LoadSharingAlgorithm"  -> result = loadSharingAlgorithmRepo.findAll();
				case "slsRange"  -> result = slsRangeRepo.findAll();
				case "encodingType"  -> result = encodingTypeRepo.findAll();
				case "gtIndicators"  -> result = gtIndicatorRepo.findAll();
				case "numberingPlan"  -> result = numberingPlanRepo.findAll();
				case "natureOfAddress"  -> result = natureOfAddressRepo.findAll();
				case "applications"  -> result = appRepository.findAll();
                default -> {
                    return ResponseMapping.errorMessageNoFound("Invalid catalog type.");
                }
            }
			
		    return ResponseMapping.successMessage("get catalog request success", result);
		} catch (Exception e) {
			log.error("get catalog request -> {}", e.getMessage());
			return ResponseMapping.exceptionMessage("get catalog request with error", e);
		}
	}
}
