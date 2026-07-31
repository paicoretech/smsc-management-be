package com.smsc.management.app.sip.service;

import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.sip.dto.RedisSipDTO;
import com.smsc.management.app.sip.mapper.SipGatewaysMapper;
import com.smsc.management.app.sip.model.entity.SipGateways;
import com.smsc.management.app.sip.model.repository.SipGatewaysRepository;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ObjectSipService {

    private final SipGatewaysRepository sipGatewaysRepository;
    private final SipGatewaysMapper sipGatewaysMapper;
    private final UtilsBase utilsBase;
    public ApiResponse refreshSipGatewaysInRedis(int networkId) {
        try {
            updateOrCreateJsonInRedis(networkId);
            return ResponseMapping.successMessage("success", null);
        } catch (Exception e) {
            log.error("Error to create JSON SIP -> {}", e.getMessage(), e);
            return ResponseMapping.exceptionMessage("Error to create JSON SIP", e);
        }
    }

    public void updateOrCreateJsonInRedis(int networkId) throws Exception {
        SipGateways gateways = sipGatewaysRepository.findByNetworkId(networkId);

        if (Objects.isNull(gateways)){
            throw new SmscBackendException("Object sip gateways cannot be null");
        }
        if (gateways.getEnabled() == Constants.DELETED_ENABLED_STATUS) {
            throw new SmscBackendException("sip gateways is deleted, cannot refresh redis");
        }
        RedisSipDTO redis = buildRedisSipDTO(gateways);
        String json = Converter.valueAsString(redis);
        utilsBase.storeInRedis(GeneralSmscConstants.SIP_GATEWAYS_HASH_NAME, Integer.toString(networkId), json);
        log.info("Sip gateways stored in Redis network_id={} -> {}", networkId, redis);
    }

    private RedisSipDTO buildRedisSipDTO(SipGateways s) {
        return sipGatewaysMapper.toRedisDTO(s);
    }

}
