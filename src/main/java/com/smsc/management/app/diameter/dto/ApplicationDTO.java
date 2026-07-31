package com.smsc.management.app.diameter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationDTO {

    private Integer id;

    private String name;

    @JsonProperty("vendor_id")
    private Integer vendorId;

    @JsonProperty("auth_appl_id")
    private Integer authApplId;

    @JsonProperty("acct_appl_id")
    private Integer acctApplId;

    private boolean delete;
}
