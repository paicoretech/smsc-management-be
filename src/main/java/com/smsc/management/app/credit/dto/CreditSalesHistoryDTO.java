package com.smsc.management.app.credit.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class CreditSalesHistoryDTO {
	@JsonIgnore
	private int id;
	
	@JsonIgnore
	private int networkId;

	
	@NotNull(message = "New cannot be null")
	@JsonProperty("credit")
	private Long credit;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonIgnore
    private LocalDateTime updatedAt;
    
    @JsonProperty("created_by")
    private String createdBy;
}
