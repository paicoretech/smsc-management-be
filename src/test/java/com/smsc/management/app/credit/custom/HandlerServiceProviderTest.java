package com.smsc.management.app.credit.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.exception.SmscBackendException;
import com.smsc.management.utils.AppProperties;
import com.smsc.management.utils.UtilsBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.paicbd.smsc.utils.RedisManager;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlerServiceProviderTest {

    @Mock
    Map<String, RedisServiceProviderDTO> cacheServiceProvider = new ConcurrentHashMap<>();

    @Mock
    RedisManager redisManager;

    @Mock
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    UtilsBase utilsBase;

    @Mock
    AppProperties appProperties;

    @InjectMocks
    HandlerServiceProvider handlerServiceProvider;

    @Test
    void removeFromCacheTest() throws Exception {
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setSystemId("System1");
        redisServiceProviderDTO.setNetworkId(1);
        getCacheServiceProviderMock().put("System1", redisServiceProviderDTO);
        assertDoesNotThrow(() -> handlerServiceProvider.removeFromCache(1));
    }

    @Test
    void getConfigForClientTestRedis() throws Exception {
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setSystemId("System1");
        redisServiceProviderDTO.setNetworkId(1);
        when(redisManager.hget(anyString(), anyString())).thenReturn(redisServiceProviderDTO.toString());
        assertNotNull(handlerServiceProvider.getConfigForClient(1));
    }

    @Test
    void getConfigForClientTestEmptyRedis() {
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setSystemId("System1");
        redisServiceProviderDTO.setNetworkId(1);
        when(redisManager.hget(anyString(), anyString())).thenReturn("");
        assertThrows(SmscBackendException.class, () -> handlerServiceProvider.getConfigForClient(1));
    }

    @Test
    void getConfigForClientTest() {
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setSystemId("System1");
        redisServiceProviderDTO.setNetworkId(1);
        when(redisManager.hget(anyString(), anyString())).thenReturn(null);
        assertThrows(SmscBackendException.class, () -> handlerServiceProvider.getConfigForClient(1));
    }

    @Test
    void updateCacheAndRedisAndSocketAllTest() {
        updateCacheAndRedisAndSocketTest(Boolean.TRUE);
        updateCacheAndRedisAndSocketTest(Boolean.FALSE);
    }

    private void updateCacheAndRedisAndSocketTest(boolean usesHttp) {
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setSystemId("System1");
        redisServiceProviderDTO.setProtocol(usesHttp ? "http" : "smpp");
        assertDoesNotThrow(() -> handlerServiceProvider.updateCacheAndRedisAndSocket(redisServiceProviderDTO));
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, RedisServiceProviderDTO> getCacheServiceProviderMock() throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = handlerServiceProvider.getClass();
        VarHandle handle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findVarHandle(clazz, "cacheServiceProvider", Map.class);
        return (ConcurrentHashMap<String, RedisServiceProviderDTO>) handle.get(handlerServiceProvider);
    }

}
