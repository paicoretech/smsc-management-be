package com.smsc.management.app.catalog.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ton_catalog")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class TonCatalog {
	@Id
	private Integer id;
	private String description;
}
