package com.smsc.management.app.credit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsedCreditByInstanceDTO {
    private int networkId;
	private int creditUsed;
}
