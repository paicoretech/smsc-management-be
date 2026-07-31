package com.smsc.management.app.catalog.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "binds_types")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class BindsTypes {
	@Id
	private String _type;

	@Column(name="use_gateway", columnDefinition = "bool NOT NULL DEFAULT true")
	private boolean useGateway = false;

	@Column(name="use_sp", columnDefinition = "bool NOT NULL DEFAULT true")
	private boolean useSp = false;
}
