package com.smsc.management.app.catalog.model.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "delivery_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class DeliveryStatus {
	@Id
	private String value;
	
	private String name;
}
