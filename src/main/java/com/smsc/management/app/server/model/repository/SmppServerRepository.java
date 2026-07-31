package com.smsc.management.app.server.model.repository;

import com.smsc.management.app.server.model.entity.SmppServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SmppServerRepository extends JpaRepository<SmppServer, Integer> {
    SmppServer findById(int id);

    SmppServer findByName(String name);

    boolean existsByIsDefaultTrue();

    @Query("SELECT ss FROM SmppServer ss ORDER BY ss.id")
    List<SmppServer> getAllSmppServers();

    @Modifying
    @Transactional
    @Query("UPDATE SmppServer ss SET ss.status = :status, ss.actionStatus = :actionStatus, ss.enabled =:enabled WHERE ss.id = :id")
    void updateStatus(@Param("id") int id, @Param("status") String status, @Param("actionStatus") String actionStatus, @Param("enabled") int enabled);
}
