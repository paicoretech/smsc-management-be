package com.smsc.management.app.settings.dto;

import lombok.Data;

@Data
public class ServerPropertiesDTO {
	private String name;
    private String ip;
    private String port;
    private String protocol;
    private String scheme;
    private String apiKey;
    private String state;
}
