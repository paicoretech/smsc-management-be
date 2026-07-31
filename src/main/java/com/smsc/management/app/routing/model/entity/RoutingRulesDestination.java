package com.smsc.management.app.routing.model.entity;

import com.smsc.management.app.sequence.SequenceNetworksId;
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
@Table(name = "routing_rules_destination")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "routing_rules_destination_id_seq", sequenceName = "routing_rules_destination_id_seq", allocationSize = 1)
public class RoutingRulesDestination {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "routing_rules_destination_id_seq")
	private int id;
	
	private int priority;
	
	@Column(name="network_id")
	private int networkId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="network_id", insertable=false, updatable=false)
	private SequenceNetworksId nextNetworkId;
	
	@Column(name="routing_rules_id")
	private int routingRulesId;
	@ManyToOne(optional = true)
	@JoinColumn(name="routing_rules_id", insertable=false, updatable=false)
	private RoutingRules routingRules;
	
	@Column(name="network_type")
	private String networkType;
}
