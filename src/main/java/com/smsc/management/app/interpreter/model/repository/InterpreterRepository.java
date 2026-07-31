package com.smsc.management.app.interpreter.model.repository;

import com.smsc.management.app.interpreter.dto.InterpreterDTO;
import com.smsc.management.app.interpreter.model.entity.Interpreter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InterpreterRepository extends JpaRepository<Interpreter, Integer> {
    Interpreter findById(int id);
    List<Interpreter> findByGatewayId(int gatewayId);

    @Query("""
        SELECT i
        FROM Interpreter i
        JOIN FETCH i.gateways gw
        ORDER BY gw.networkId DESC
    """)
    List<Interpreter> getAllInterpreters();

    boolean existsByPathAndGatewayIdNotAndDefaultTemplateIsFalse(String path, int gatewayId);

    void deleteAllByGatewayId(int gatewayId);
}
