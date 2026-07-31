package com.smsc.management.app.routing.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "custom_param_matcher")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "custom_param_matcher_id_seq", sequenceName = "custom_param_matcher_id_seq", allocationSize = 1)
public class CustomParamMatcher {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "custom_param_matcher_id_seq")
    private int id;

    @Column(name="property_name")
    private String propertyName;

    @Column(name="value_matcher")
    private String valueMatcher;

    @Column(name="routing_rule_id")
    private int  routingRuleId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="routing_rule_id", insertable=false, updatable=false)
    private RoutingRules routingRules;
}
