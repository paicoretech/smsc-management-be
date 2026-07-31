package com.smsc.management.app.errorcode.model.entity;

import com.smsc.management.app.catalog.model.entity.DeliveryStatus;
import com.smsc.management.utils.EntityBase;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "error_code_mapping", uniqueConstraints = {@UniqueConstraint(columnNames={"error_code_id", "delivery_error_code_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "error_code_mapping_id_seq", sequenceName = "error_code_mapping_id_seq", allocationSize = 1)
public class ErrorCodeMapping extends EntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_code_mapping_id_seq")
	private int id;
	
	@Column(name = "error_code_id")
	private int errorCodeId;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="error_code_id", insertable=false, updatable=false)
	private ErrorCode errorCode;
	
	@Column(name = "delivery_error_code_id")
	private int deliveryErrorCodeId;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="delivery_error_code_id", insertable=false, updatable=false)
	private DeliveryErrorCode deliveryErrorCode;
	
	@Column(name = "delivery_status_id")
	private String deliveryStatusId;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="delivery_status_id", insertable=false, updatable=false)
	private DeliveryStatus deliveryStatus;
}
