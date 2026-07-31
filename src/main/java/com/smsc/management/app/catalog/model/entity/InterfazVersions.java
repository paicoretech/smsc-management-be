package com.smsc.management.app.catalog.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interfaz_versions")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class InterfazVersions {
	@Id
	private String id;
	private String version;
}
