package com.smsc.management.app.routing.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routing_rules_action_advanced")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoutingRulesActionAdvanced {
    @Id
    @Column(name = "routing_rules_id")
    private int routingRulesId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="routing_rules_id", insertable=false, updatable=false)
    private RoutingRules routingRules;

    @Column(name = "map_version", nullable = false)
    private Integer mapVersion;

    @Column(name = "operation_code_sri", nullable = false)
    private Integer operationCodeSri;

    @Column(name = "operation_code_mt", nullable = false)
    private Integer operationCodeMt;

    @Column(name = "ssn_smsc_sri", nullable = false)
    private Integer ssnSmscSri;

    @Column(name = "ssn_hlr_sri", nullable = false)
    private Integer ssnHlrSri;

    @Column(name = "ssn_msc_mt", nullable = false)
    private Integer ssnMscMt;

    @Column(name = "ssn_smsc_mt", nullable = false)
    private Integer ssnSmscMt;

    @Column(name = "sccp_source_address_sri", nullable = false)
    private String sccpSourceAddressSri;

    @Column(name = "sccp_source_address_mt", nullable = false)
    private String sccpSourceAddressMt;

    @Column(name = "sccp_destination_address_mt" ,columnDefinition = "text NOT NULL DEFAULT ''")
    private String sccpDestinationAddressMt;

    @Column(name = "sccp_destination_address_sri", columnDefinition = "text NOT NULL DEFAULT ''")
    private String sccpDestinationAddressSri;

    @Column(name = "custom_map_layer_source_address_sri", columnDefinition = "text NOT NULL DEFAULT ''")
    private String customMapLayerSourceAddressSri;

    @Column(name = "custom_map_layer_source_address_mt", columnDefinition = "text NOT NULL DEFAULT ''")
    private String customMapLayerSourceAddressMt;

    @Column(name = "custom_map_layer_service_centre_address_oa", columnDefinition = "text NOT NULL DEFAULT ''")
    private String customMapLayerServiceCentreAddressOa;

    @Column(name = "priority_flag_sri", nullable = false)
    private boolean priorityFlagSri;

    @Column(name = "application_context_mt", nullable = false)
    private String applicationContextMt;
}
