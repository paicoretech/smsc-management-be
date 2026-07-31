package com.smsc.management.app.catalog.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "encoding_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class EncodingType {
	@Id
	public Integer id;
	
	public String name;
}
