package com.smsc.management.app.gateway.model.repository;

import com.paicbd.smsc.utils.Generated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class GatewaySearchCriteria {
    private String ip;
    private int port;
    private String systemType;
    private String interfaceVersion;
    private String bindType;
    private int enabled;
    private int networkId;
    private String systemId;
}
