package com.smsc.management.app.provider.model.repository;

import com.smsc.management.app.provider.model.entity.ServiceProvider;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Integer> {
    ServiceProvider findById(int id);
    
    ServiceProvider findByNetworkIdAndEnabledNot(int id, int enabled);

    ServiceProvider findByExternalIdAndEnabledNot(String id, int enabled);

    List<ServiceProvider> findByEnabledNot(int enabled);

    List<ServiceProvider> findBySystemIdAndEnabledNot(String systemId, int enabled);

    List<ServiceProvider> findBySystemIdAndEnabledNotAndNetworkIdNot(String systemId, int enabled, int networkId);

    ServiceProvider findBySystemId(String systemId);
    ServiceProvider findByNetworkId(int networkId);

    ServiceProvider findByExternalId(String externalId);

    boolean existsByNetworkIdAndProtocolAndEnabledNot(int networkId, String protocol, int enabled);

    ServiceProvider findByNetworkIdAndProtocolAndEnabledNot(int networkId, String protocol, int enabled);

    @Modifying
    @Transactional
    @Query("UPDATE ServiceProvider sp SET sp.balance = :balance WHERE sp.networkId = :networkId")
    int saveBalance(@Param("networkId") int networkId, @Param("balance") long balance);
    
    @Modifying
    @Transactional
    @Query("UPDATE ServiceProvider sp SET sp.status = :status WHERE sp.networkId = :networkId")
    void saveStatusSp(@Param("networkId") int networkId, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE ServiceProvider sp SET sp.activeSessionsNumbers = :nSessions WHERE sp.networkId = :networkId")
    void saveSessionsSp(@Param("networkId") int networkId, @Param("nSessions") int nSessions);

    @Modifying
    @Transactional
    @Query("""
            UPDATE ServiceProvider sp SET sp.activeSessionsNumbers = 0, sp.enabled = 0, sp.status = 'STOPPED'
            WHERE sp.networkId = :networkId
            """)
    void updateActiveSessionToZeroAndEnabledToZero(@Param("networkId") int networkId);

    @Query("SELECT COUNT(sp) FROM ServiceProvider sp WHERE sp.enabled != 2 and LENGTH(sp.password) > :passwordLength")
    int countByPasswordLengthGreaterThan(int passwordLength);

    @Query("SELECT COUNT(sp) FROM ServiceProvider sp WHERE sp.enabled != 2 and LENGTH(sp.systemId) > :systemIdLength")
    int countBySystemIdLengthGreaterThan(int systemIdLength);
}