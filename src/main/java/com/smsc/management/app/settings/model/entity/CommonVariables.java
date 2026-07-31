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
@Table(name = "common_variables")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class CommonVariables {
	@Id
	private String key;
	
	private String value;
	
	@Column(name = "data_type")
	private String dataType;
	
	@Column(name = "redis_replicated")
	private Boolean redisReplicated = false;

}
