package com.smsc.management.app.settings.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "General_smsc_retry")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class GeneralSmscRetry {
	@Id
	private int id;
	
	@Column(name = "first_retry_delay")
	private int firstRetryDelay = 10;
	
	@Column(name = "max_due_delay")
	private int maxDueDelay;
	
	@Column(name = "retry_delay_multiplier")
	private int retryDelayMultiplier;
}
