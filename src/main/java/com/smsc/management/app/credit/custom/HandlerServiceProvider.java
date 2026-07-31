package com.smsc.management.app.credit.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.AppProperties;
import org.springframework.stereotype.Component;

import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.utils.UtilsBase;
import static com.smsc.management.utils.Constants.UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT;
import static com.smsc.management.utils.Constants.UPDATE_SERVICE_HTTP_PROVIDER_ENDPOINT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.paicbd.smsc.utils.RedisManager;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandlerServiceProvider {
	private final Map<Integer, RedisServiceProviderDTO> cacheServiceProvider = new ConcurrentHashMap<>();
	private final RedisManager redisManager;
	private final UtilsBase utilsBase;

	private final AppProperties appProperties;
		
	/**
     * Add and update a service provider configuration to the cache.
     *
     * @param networkId The system ID of the service provider
     * @param config   The service provider configuration
     */
	public void addToCache(int networkId, RedisServiceProviderDTO config) {
		cacheServiceProvider.put(networkId, config);
		log.info("Added service provider to cache -> {}", config.toString());
    }
	
	/**
     * Removes a service provider configuration from the cache.
     *
     * @param networkId The system ID of the service provider to remove
     */
	public void removeFromCache(int networkId) {
		cacheServiceProvider.remove(networkId);
		log.warn("service provider with system ID {} removed.", networkId);
    }
	
	/**
     * Retrieves the configuration for a specific service provider.
     *
     * @param networkId The system ID of the service provider
     * @return The service provider configuration
     * @throws JsonProcessingException if the readValue method throws and exception
     */
	public RedisServiceProviderDTO getConfigForClient(int networkId)  throws JsonProcessingException {
		RedisServiceProviderDTO spData = cacheServiceProvider.get(networkId);
		
		if (spData == null) {
			String spStr = redisManager.hget(GeneralSmscConstants.SERVICE_PROVIDERS_HASH_NAME, String.valueOf(networkId));
			if (spStr == null || spStr.isBlank()) {
				throw new SmscBackendException("System ID " + networkId + " was not found");
			}
			spData = Converter.stringToObject(spStr, RedisServiceProviderDTO.class);
        	this.addToCache(networkId, spData);
		}
		
		return spData;
    }
	
	/**
	 * Updates the cache, Redis, and socket with the provided Redis service provider configuration.
	 *
	 * @param config The Redis service provider configuration to update in the cache, Redis, and socket.
	 */
	public void updateCacheAndRedisAndSocket(RedisServiceProviderDTO config) {
		// update local current hash map
		cacheServiceProvider.put(config.getNetworkId(), config);
		
		// update Redis
		utilsBase.storeInRedis(GeneralSmscConstants.SERVICE_PROVIDERS_HASH_NAME, String.valueOf(config.getNetworkId()), config.toString());
		
		// send notify by socket by protocol
		String endpoint = UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT;
		if ("http".equalsIgnoreCase(config.getProtocol())) {
			endpoint = UPDATE_SERVICE_HTTP_PROVIDER_ENDPOINT;
		}
        utilsBase.sendNotificationSocket(endpoint, String.valueOf(config.getNetworkId()));
	}
}
