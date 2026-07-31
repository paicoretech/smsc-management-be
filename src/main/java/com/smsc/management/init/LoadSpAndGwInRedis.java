package com.smsc.management.init;

import java.util.List;

import org.springframework.context.annotation.Configuration;

import com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO;
import com.smsc.management.app.gateway.model.entity.Gateways;
import com.smsc.management.app.mno.model.entity.OperatorMno;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.app.sip.service.ObjectSipService;
import com.smsc.management.app.ss7.model.entity.Ss7Gateways;
import com.smsc.management.app.gateway.model.repository.GatewaysRepository;
import com.smsc.management.app.mno.model.repository.OperatorMnoRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.ss7.model.repository.Ss7GatewaysRepository;
import com.smsc.management.app.errorcode.service.ErrorCodeMappingService;
import com.smsc.management.app.gateway.service.GatewaysService;
import com.smsc.management.app.routing.service.RoutingRulesService;
import com.smsc.management.app.provider.service.ServiceProviderService;
import com.smsc.management.app.ss7.service.ObjectSs7Service;
import com.smsc.management.utils.Constants;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LoadSpAndGwInRedis {
	private final GatewaysRepository gatewaysRepo;
	private final ServiceProviderRepository spRepo;
	private final Ss7GatewaysRepository ss7GwRepo;
	private final SipGatewaysRepository sipGwRepo;
	private final OperatorMnoRepository opMnoRepo;
	private final RoutingRulesRepository routingRulesRepo;
	private final ServiceProviderService processSp;
	private final GatewaysService processGw;
	private final ObjectSs7Service processObjSs7;
	private final ObjectSipService processObjSip;
	private final ErrorCodeMappingService processError;
	private final RoutingRulesService routingRulesService;
	
	@PostConstruct
	private void init() {
		try {
			loadSp();
		} catch (Exception e) {
			log.error("Error to load init Service provider in Redis -> {}", e.getMessage());
		}
		
		try {
			loadGw();
		} catch (Exception e) {
			log.error("Error to load init Gateways HTTP/SMPP in Redis -> {}", e.getMessage());
		}
		
		try {
			loadSs7Gw();
		} catch (Exception e) {
			log.error("Error to load init Gateways SS7 in Redis -> {}", e.getMessage());
		}

		try {
			loadSipGw();
		} catch (Exception e) {
			log.error("Error to load init Gateways SIP in Redis -> {}", e.getMessage());
		}

		try {
			loadErrorMapping();
		} catch (Exception e) {
			log.error("Error to load init Error mapping in Redis -> {}", e.getMessage());
		}
		
		try {
			loadRoutingRules();
		} catch (Exception e) {
			log.error("Error to load init Routing Rules in Redis -> {}", e.getMessage());
		}
	}
	
	public void loadSp() {
		List<ServiceProvider> sps = spRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS);
		if (sps != null) {
			for (ServiceProvider sp : sps) {
				processSp.socketAndRedisAction(sp.getNetworkId());
			}
		}
	}
	
	private void loadGw() {
		List<Gateways> gateways = gatewaysRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS);
		if (gateways != null) {
			for (Gateways gw : gateways) {
				processGw.onlyToLoadInitInRedisAndSocket(gw.getNetworkId());
			}
		}
	}
	
	private void loadSs7Gw() {
		List<Ss7Gateways> ss7Gws = ss7GwRepo.findByEnabledNotAndAllowedTraffic(Constants.DELETED_ENABLED_STATUS, Constants.SS7_ALLOWED_TRAFFIC);
		if (ss7Gws != null) {
			for (Ss7Gateways gw : ss7Gws) {
				try {
					processObjSs7.updateOrCreateJsonInRedis(gw.getNetworkId());
				} catch (Exception e) {
					log.error("Error to load data init for ss7 gateway {} -> {}", gw.getName(), e.getMessage());
				}
			}
		}
	}

	private void loadSipGw() {
		List<SipGateways> sipGws = sipGwRepo.findByEnabledNot(Constants.DELETED_ENABLED_STATUS);
		if (sipGws != null) {
			for (SipGateways gw : sipGws) {
				try {
					processObjSip.updateOrCreateJsonInRedis(gw.getNetworkId());
				} catch (Exception e) {
					log.error("Error to load data init for sip gateway {} -> {}", gw.getName(), e.getMessage());
				}
			}
		}
	}
	
	private void loadErrorMapping() {
		List<OperatorMno> mnos = opMnoRepo.findByEnabledTrue();
		if (mnos != null) {
			for (OperatorMno mno : mnos) {
				processError.socketAndRedisAction(mno.getId());
			}
		}
		
	}
	
	private void loadRoutingRules() {
		List<NetworksToRoutingRulesDTO> networks = routingRulesRepo.findGatewayNamesAndIds();
		if (networks != null && !networks.isEmpty()) {
			for (NetworksToRoutingRulesDTO network : networks) {
				routingRulesService.socketAndRedisAction(network.getNetworkId());
			}
		}
	}
}
