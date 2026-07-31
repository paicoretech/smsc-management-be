package com.smsc.management.app.mno.model.entity;

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
@Table(name = "operator_mno")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "operator_mno_id_seq", sequenceName = "operator_mno_id_seq", allocationSize = 1)
public class OperatorMno extends EntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operator_mno_id_seq")
	private Integer id;

	@Column(unique = true)
	private String name;
	
	@Column(name = "tlv_message_receipt_id")
	private boolean tlvMessageReceiptId;
	
	@Column(name = "message_id_decimal_format")
	private boolean messageIdDecimalFormat;
	
	@Column(name = "enabled", columnDefinition = "boolean default true")
	private boolean enabled = true;
}
