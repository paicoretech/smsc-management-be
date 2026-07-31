package com.smsc.management.app.errorcode.model.entity;

import com.smsc.management.app.mno.model.entity.OperatorMno;
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
@Table(name = "error_code", uniqueConstraints = {@UniqueConstraint(columnNames={"code", "mno_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "error_code_id_seq", sequenceName = "error_code_id_seq", allocationSize = 1)
public class ErrorCode extends EntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_code_id_seq")
	private int id;
	
	private String code;
	@Column(nullable = false)
	private String description;
	
	@Column(name="mno_id")
	private int mnoId;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="mno_id", insertable=false, updatable=false)
	private OperatorMno operatorMno;
}
