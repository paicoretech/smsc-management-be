package com.smsc.management.app.mno.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OperatorMNODTO {
	private int id;
	
	@NotBlank(message = "Provider name cannot be empty")
	@NotNull(message = "Provider name cannot be null")
	private String name;
	
	@JsonProperty("created_at")
	private LocalDateTime createdAt;
	
	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;
	
	@NotNull(message = "TLV Receipt Message Id parameter cannot be null")
	@JsonProperty("tlv_message_receipt_id")
    private boolean tlvMessageReceiptId;
	
	@NotNull(message = "Message Id Decimal Format parameter cannot be null")
	@JsonProperty("message_id_decimal_format")
    private boolean messageIdDecimalFormat;

	public OperatorMNODTO(
			@NotBlank(message = "Provider name cannot be empty") @NotNull(message = "Provider name cannot be null") String name
		) {
		this.name = name;
	}
}
