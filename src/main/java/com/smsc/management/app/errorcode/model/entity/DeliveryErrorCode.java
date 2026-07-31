package com.smsc.management.app.errorcode.model.entity;

import com.smsc.management.utils.EntityBase;

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
@Table(name = "delivery_error_code")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "delivery_error_code_id_seq", sequenceName = "delivery_error_code_id_seq", allocationSize = 1)
public class DeliveryErrorCode extends EntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_error_code_id_seq")
	private int id;
	
	@Column(unique = true)
	private String code;
	
	@Column(nullable = false)
	private String description;
}
