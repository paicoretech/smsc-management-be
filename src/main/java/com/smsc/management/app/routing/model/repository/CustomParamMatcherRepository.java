package com.smsc.management.app.routing.model.repository;

import com.smsc.management.app.routing.dto.CustomParamMatcherDTO;
import com.smsc.management.app.routing.model.entity.CustomParamMatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomParamMatcherRepository extends JpaRepository<CustomParamMatcher, Integer>  {
    @Query("SELECT new com.smsc.management.app.routing.dto.CustomParamMatcherDTO(cm.propertyName, cm.valueMatcher) " +
            " FROM RoutingRules rr INNER JOIN CustomParamMatcher cm " +
            " ON rr.id = cm.routingRuleId " +
            " WHERE rr.id = :routingRuleId " +
            " ORDER BY cm.id")
    List<CustomParamMatcherDTO> findAllByRoutingRuleId(int routingRuleId);

    void deleteAllByRoutingRuleId(int routingRuleId);
}
