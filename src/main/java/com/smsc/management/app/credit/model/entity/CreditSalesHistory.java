package com.smsc.management.app.credit.model.entity;

import com.smsc.management.app.provider.model.entity.ServiceProvider;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credit_sales_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "credit_sales_history_id_seq", sequenceName = "credit_sales_history_id_seq", allocationSize = 1)
public class CreditSalesHistory extends EntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credit_sales_history_id_seq")
	private int id;
	
	@Column(name="network_id")
	private int networkId;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name="network_id", insertable=false, updatable=false)
	private ServiceProvider serviceProvider;
	
	@Column(columnDefinition = "bigint DEFAULT 0")
	private Long credit;
	
	@Column(name="description")
	private String description;
}
