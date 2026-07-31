package com.smsc.management.app.credit.custom;

import com.smsc.management.app.provider.dto.RedisServiceProviderDTO;
import com.smsc.management.app.provider.mapper.ServiceProviderMapper;
import com.smsc.management.app.provider.model.entity.ServiceProvider;
import com.smsc.management.app.provider.model.repository.ServiceProviderRepository;
import com.smsc.management.app.settings.model.entity.CommonVariables;
import com.smsc.management.app.settings.model.repository.CommonVariablesRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.smsc.management.utils.Constants.LOCAL_CHARGING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class HandlerCreditByServiceProviderTest {

    @Mock
    UtilsBase utilsBase;

    @Mock
    RedisManager redisManager;

    @Mock
    ServiceProviderRepository serviceProviderRepo;

    @Mock
    HandlerServiceProvider handlerSp;

    @Mock
    ServiceProviderMapper serviceProviderMapper;

    @Mock
    CommonVariablesRepository commonVariableRepo;

    @Mock
    AppProperties appProperties;

    @InjectMocks
    HandlerCreditByServiceProvider handlerCreditByServiceProvider;

    @Test
    void testInitAllTests() {
        testInit();
        testInitCatchPart();
        testInitCommonVariables();
    }

    @Test
    void putLocalChargingAllTests() {
        testPutLocalCharging(null);
        testPutLocalCharging(" ");
    }

    @Test
    void removeFromCacheTest() {
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.removeFromCache(1));
    }

    @Test
    void getCurrentBalanceAllTests() throws Exception {
        getCurrentBalanceTest(1);
        getCurrentBalanceTest(21);
    }

    @Test
    void addCreditPackageAllTests() throws Exception {
        addCreditPackageTest(0L, 5000L);
        addCreditPackageTest(-1L, 5000L);
        addCreditPackageTest(-1L, 2L);
        addCreditPackageTest(-1L, -2L);
    }

    @Test
    void storeBalanceInCacheAllTests() throws Exception {
        storeBalanceInCacheNegativeBalance(true);
        storeBalanceInCacheNegativeBalance(false);
    }

    @Test
    void storeBalanceInCacheAnyException() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSystemId("System1");
        serviceProvider.setBalance(1_000_000L);
        when(serviceProviderMapper.toServiceProviderDTO(any())).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> handlerCreditByServiceProvider.storeBalanceInCache(serviceProvider));
    }

    @Test
    void decreaseCreditUsedAllTests() {
        decreaseCreditUsed(1_000_000L, true);
        decreaseCreditUsed(-1_000_000L, true);
        decreaseCreditUsed(-1_000_000L, false);
        decreaseCreditUsed(1_000_000L, false);
    }

    @Test
    void decreaseCreditUsedAnyException() {
        try {
            when(handlerSp.getConfigForClient(anyInt())).thenThrow(new RuntimeException());
            assertThrows(RuntimeException.class, () -> handlerCreditByServiceProvider.decreaseCreditUsed(1, 0L));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    void updateHasAvailableAllTests() throws Exception {
        updateHasAvailableCredit(Boolean.FALSE, Boolean.FALSE, true);
        updateHasAvailableCredit(Boolean.TRUE, Boolean.FALSE, false);
        updateHasAvailableCredit(Boolean.TRUE, Boolean.TRUE, false);
        updateHasAvailableCredit(Boolean.FALSE, Boolean.TRUE, true);
    }


    @Test
    void updateDatabaseTest() throws Exception {
        when(handlerSp.getConfigForClient(anyInt())).thenReturn(new RedisServiceProviderDTO());
        when(serviceProviderRepo.findByNetworkIdAndEnabledNot(anyInt(), anyInt())).thenReturn(new ServiceProvider());
        when(serviceProviderRepo.saveBalance(anyInt(), anyLong())).thenReturn(0);
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.updateDataBase(1, 0L));
    }

    @Test
    void updateDatabaseNoRecordsUpdatedTest() throws Exception {
        when(handlerSp.getConfigForClient(anyInt())).thenThrow(new RuntimeException());
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.updateDataBase(0, 0L));
    }

    @Test
    void getLastBalanceTest() {
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.getLastBalance(1));
    }

    @Test
    void setLastBalanceTest() {
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.setLastBalance(1, 0L));
    }

    private void decreaseCreditUsed(long valueToDecrease, boolean isLocalCharging) {
        try {
            getLocalChargingMock().put(LOCAL_CHARGING, isLocalCharging);
            if (valueToDecrease > 0 && isLocalCharging) {
                when(handlerSp.getConfigForClient(anyInt())).thenReturn(new RedisServiceProviderDTO());
            }
            assertTrue(handlerCreditByServiceProvider.decreaseCreditUsed(12, valueToDecrease));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void testPutLocalCharging(String value) {
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.putLocalCharging(value));
    }

    private void updateHasAvailableCredit(boolean currentHasAvailableCredit, boolean newHasAvailableCredit, boolean isLocalCharging) throws Exception {
        getLocalChargingMock().put(LOCAL_CHARGING, isLocalCharging);
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setHasAvailableCredit(currentHasAvailableCredit);
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.updateHasAvailableCredit(redisServiceProviderDTO, newHasAvailableCredit));
    }

    private void storeBalanceInCacheNegativeBalance(boolean localCharging) throws IllegalAccessException, NoSuchFieldException {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSystemId("System1");
        serviceProvider.setBalance(-10L);
        getLocalChargingMock().put(LOCAL_CHARGING, localCharging);
        when(serviceProviderMapper.toServiceProviderDTO(any())).thenReturn(new RedisServiceProviderDTO());
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.storeBalanceInCache(serviceProvider));
    }

    private void addCreditPackageTest(long currentBalance, long newBalance) throws Exception {
        int networkId = 1;
        getBalanceMock("balance").put(networkId, new AtomicLong(currentBalance));
        getBalanceMock("lastBalance").put(networkId, new AtomicLong(1_000_000L));
        RedisServiceProviderDTO redisServiceProviderDTO = new RedisServiceProviderDTO();
        redisServiceProviderDTO.setHasAvailableCredit(true);
        when(handlerSp.getConfigForClient(networkId)).thenReturn(redisServiceProviderDTO);
        assertTrue(handlerCreditByServiceProvider.addCreditPackage(networkId, newBalance));
    }

    private void getCurrentBalanceTest(int balanceNetworkId) throws Exception {
        int currentNetworkId = 1;
        getBalanceMock("balance").put(currentNetworkId, new AtomicLong(1L));
        if (currentNetworkId == balanceNetworkId) {
            assertNotNull(handlerCreditByServiceProvider.getCurrentBalance(balanceNetworkId));
        } else {
            assertNull(handlerCreditByServiceProvider.getCurrentBalance(balanceNetworkId));
        }
    }

    private void testInit() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSystemId("System1");
        serviceProvider.setBalance(1_000_000L);
        RedisServiceProviderDTO serviceProviderDTO = new RedisServiceProviderDTO();
        serviceProviderDTO.setSystemId("System1");
        when(serviceProviderRepo.findByEnabledNot(2)).thenReturn(List.of(serviceProvider));
        when(serviceProviderMapper.toServiceProviderDTO(serviceProvider)).thenReturn(serviceProviderDTO);
        when(commonVariableRepo.findByKey(LOCAL_CHARGING)).thenReturn(null);
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.init());
    }

    private void testInitCatchPart() {
        List<ServiceProvider> serviceProviders = new ArrayList<>();
        when(serviceProviderRepo.findByEnabledNot(anyInt()))
                .thenThrow(new RuntimeException())
                .thenReturn(serviceProviders);
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.init());
    }

    private void testInitCommonVariables() {
        CommonVariables commonVariables = new CommonVariables();
        commonVariables.setKey("key");
        commonVariables.setValue("true");
        when(commonVariableRepo.findByKey(LOCAL_CHARGING)).thenReturn(commonVariables);
        assertDoesNotThrow(() -> handlerCreditByServiceProvider.init());
    }


    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, Boolean> getLocalChargingMock() throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = handlerCreditByServiceProvider.getClass();
        VarHandle handle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findVarHandle(clazz, "localCharging", Map.class);
        return (ConcurrentHashMap<String, Boolean>) handle.get(handlerCreditByServiceProvider);
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Integer, AtomicLong> getBalanceMock(String mapName) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = handlerCreditByServiceProvider.getClass();
        VarHandle handle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findVarHandle(clazz, mapName, Map.class);
        return (ConcurrentHashMap<Integer, AtomicLong>) handle.get(handlerCreditByServiceProvider);
    }
}
