package com.smsc.management.app.routing.model.repository;

import com.smsc.management.app.routing.model.entity.RoutingRulesActionAdvanced;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutingRulesActionAdvancedRepository extends JpaRepository<RoutingRulesActionAdvanced, Integer> {
    RoutingRulesActionAdvanced findFirstByRoutingRulesId(Integer routingRuleId);

    void deleteByRoutingRulesId(Integer routingRuleId);
}
