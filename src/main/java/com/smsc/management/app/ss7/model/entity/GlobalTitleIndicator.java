package com.smsc.management.app.ss7.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "global_title_indicator")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class GlobalTitleIndicator {
	@Id
	@Column(name = "gt_indicator_id")
	private String gtIndicatorId;
	
	@Column(name = "gt_indicator")
	private String gtIndicator;
}
