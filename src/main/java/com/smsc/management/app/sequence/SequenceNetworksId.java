package com.smsc.management.app.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sequence_networks_id")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "networks_id_seq", sequenceName = "networks_id_seq", allocationSize = 1, initialValue = 1)
public class SequenceNetworksId {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "networks_id_seq")
	private int id;
	
	@Column(name="network_type")
	private String networkType;
	
	public SequenceNetworksId(String networkType) {
		this.networkType = networkType;
	}
}
