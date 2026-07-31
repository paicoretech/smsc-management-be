package com.smsc.management.app.gateway.model.repository;

import java.util.List;

import com.smsc.management.app.interpreter.dto.HttpGatewaysDTO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.smsc.management.app.gateway.model.entity.Gateways;

public interface GatewaysRepository extends CrudRepository<Gateways, Integer> {
    Gateways findById(int id);

    List<Gateways> findByEnabledNot(int enabled);

    List<Gateways> findAllBySystemIdAndEnabledNot(String systemId, int enabled);

    List<Gateways> findBySystemIdAndEnabledNotAndNetworkIdNot(String systemId, int enabled, int networkId);

    Gateways findBySystemIdAndEnabledNot(String systemId, int enabled);

    Gateways findByNetworkIdAndEnabledNot(int systemId, int enabled);

    List<Gateways> findByMnoIdAndEnabledNot(int mnoId, int enabled);

    List<Gateways> findAllByIpAndPortAndSystemTypeAndInterfaceVersionAndEnabledNot(String ip, int port, String systemType, String interfaceVersion, int enabled);

    boolean existsByIpAndPortAndSystemTypeAndInterfaceVersionAndBindTypeAndEnabledNotAndSystemId(String ip, int port, String systemType, String interfaceVersion, String bindType, int enabled, String systemId);

    boolean existsByIpAndEnabledNotAndSystemId(String ip, int enabled, String systemId);

    @Query("""
            SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END
                   FROM Gateways g
                   WHERE g.ip =  :#{#criteria.ip}
                   AND g.port =  :#{#criteria.port}
                   AND g.systemType =  :#{#criteria.systemType}
                   AND g.interfaceVersion =  :#{#criteria.interfaceVersion}
                   AND g.bindType =  :#{#criteria.bindType}
                   AND g.enabled <>  :#{#criteria.enabled}
                   AND g.networkId <>  :#{#criteria.networkId}
                   AND g.systemId =  :#{#criteria.systemId}
            """
    )
    boolean existsByGatewaySearchCriteria(@Param("criteria") GatewaySearchCriteria criteria);

    boolean existsByIpAndEnabledNotAndNetworkIdNotAndSystemId(String ip, int enabled, int networkId, String systemId);

    Gateways findByExternalId(String externalId);

    @Modifying
    @Transactional
    @Query("UPDATE Gateways sp SET sp.status = :status WHERE sp.networkId = :networkId")
    int saveStatusGateway(@Param("networkId") int networkId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Gateways sp SET sp.activeSessionsNumbers = CASE " +
           "WHEN (:nSessions = 1 AND sp.activeSessionsNumbers >= 0 AND sp.activeSessionsNumbers < sp.sessionsNumber) THEN sp.activeSessionsNumbers + 1 " +
           "WHEN (:nSessions = -1 AND sp.activeSessionsNumbers > 0) THEN sp.activeSessionsNumbers - 1 " +
           "WHEN (:nSessions = 0 ) THEN 0 " +
           "ELSE sp.activeSessionsNumbers END " +
           "WHERE sp.networkId = :networkId")
    int saveSessionsGateway(@Param("networkId") int networkId, @Param("nSessions") int nSessions);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Gateways sp SET sp.activeSessionsNumbers = 0, sp.enabled = 0, sp.status = 'STOPPED'
            WHERE sp.networkId = :networkId
            """)
    void updateGatewayToStatusStoppedEnabledStoppedAndSessionsZero(@Param("networkId") int networkId);

    @Query("SELECT COUNT(gw) FROM Gateways gw WHERE gw.enabled != 2 and LENGTH(gw.password) > :passwordLength")
    int countByPasswordLengthGreaterThan(int passwordLength);

    @Query("SELECT COUNT(gw) FROM Gateways gw WHERE gw.enabled != 2 and LENGTH(gw.systemId) > :systemIdLength")
    int countBySystemIdLengthGreaterThan(int systemIdLength);

    boolean existsByEnabledNotAndNetworkIdAndProtocol(int enabled, int networkId, String protocol);

    @Query("SELECT NEW com.smsc.management.app.interpreter.dto.HttpGatewaysDTO(f.networkId, f.name) " +
           "FROM Gateways f WHERE f.enabled != 2 AND f.protocol = 'HTTP'" +
           "ORDER BY f.networkId")
    List<HttpGatewaysDTO> findHttpGatewaysList();

    Gateways findByNetworkIdAndProtocol(int networkId, String protocol);
}
