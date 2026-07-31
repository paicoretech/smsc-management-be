package com.smsc.management.app.routing.model.entity;

import com.smsc.management.app.catalog.model.entity.NpiCatalog;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.app.catalog.model.entity.TonCatalog;
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
@Table(name = "routing_rules")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "routing_rules_id_seq", sequenceName = "routing_rules_id_seq", allocationSize = 1)
public class RoutingRules {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "routing_rules_id_seq")
	private int id;
	
	@Column(name="origin_network_id")
	private int  originNetworkId;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="origin_network_id", insertable=false, updatable=false)
	private SequenceNetworksId sourceNetworkId;
	
	@Column(name="regex_source_addr")
	private String  regexSourceAddr;
	
	@Column(name="regex_source_addr_ton")
	private String  regexSourceAddrTon;
	
	@Column(name="regex_source_addr_npi")
	private String  regexSourceAddrNpi;
	
	@Column(name="regex_destination_addr")
	private String  regexDestinationAddr;
	
	@Column(name="regex_dest_addr_ton")
	private String  regexDestAddrTon;
	
	@Column(name="regex_dest_addr_npi")
	private String  regexDestAddrNpi;
	
	@Column(name="regex_imsi_digits_mask")
	private String  regexImsiDigitsMask;
	
	@Column(name="regex_network_node_number")
	private String  regexNetworkNodeNumber;
	
	@Column(name="regex_calling_party_address")
	private String  regexCallingPartyAddress;
	
	@Column(name="is_sri_response", columnDefinition = "boolean default false")
	private boolean isSriResponse;

	@Column(name="regex_short_message")
	private String  regexShortMessage;
	
	// actions
	@Column(name="new_source_addr")
	private String  newSourceAddr;
	
	@Column(name="new_source_addr_ton")
	private int  newSourceAddrTon;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="new_source_addr_ton", insertable=false, updatable=false)
	private TonCatalog tonCatalog;
	
	@Column(name="new_source_addr_npi")
	private int  newSourceAddrNpi;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="new_source_addr_npi", insertable=false, updatable=false)
	private NpiCatalog npicatalog;
	
	@Column(name="new_destination_addr")
	private String  newDestinationAddr;
	
	@Column(name="new_dest_addr_ton")
	private int  newDestAddrTon;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="new_dest_addr_ton", insertable=false, updatable=false)
	private TonCatalog tonCatalogDes;
	
	@Column(name="new_dest_addr_npi")
	private int  newDestAddrNpi;
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="new_dest_addr_npi", insertable=false, updatable=false)
	private NpiCatalog npiCatalogDest;
	
	@Column(name="add_source_addr_prefix")
	private String  addSourceAddrPrefix;
	
	@Column(name="add_dest_addr_prefix")
	private String  addDestAddrPrefix;
	
	@Column(name="remove_source_addr_prefix")
	private String  removeSourceAddrPrefix;
	
	@Column(name="remove_dest_addr_prefix")
	private String  removeDestAddrPrefix;
	
	@Column(name="new_gt_sccp_addr")
	private String  newGtSccpAddr;
	
	@Column(name="drop_map_sri", columnDefinition = "boolean default false")
	private boolean  dropMapSri;
	
	@Column(name="network_id_to_map_sri")
	private Integer  networkIdToMapSri;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="network_id_to_map_sri", insertable=false, updatable=false)
	private SequenceNetworksId networksMapSri;
	
	@Column(name="network_id_to_permanent_failure")
	private Integer  networkIdToPermanentFailure;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="network_id_to_permanent_failure", insertable=false, updatable=false)
	private SequenceNetworksId networksPermanentFailure;
	
	@Column(name="drop_temp_failure")
	private boolean  dropTempFailure;
	
	@Column(name="network_id_temp_failure")
	private Integer  networkIdTempFailure;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="network_id_temp_failure", insertable=false, updatable=false)
	private SequenceNetworksId networksTempFailure;
	
	@Column(name="check_sri_response", columnDefinition = "boolean default false")
	private boolean checkSriResponse;

	@Column(name="new_short_message")
	private String  newShortMessage;

	@Column(name= "diameter_charging")
	private boolean diameterCharging;

	@Column(name = "apply_for_refund")
	private boolean applyForRefund = false;
}
