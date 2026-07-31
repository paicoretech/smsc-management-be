package com.smsc.management.app.ss7.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smsc.management.utils.StaticMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class RedisSs7DTO {
	private String name;
	@JsonProperty("network_id")
	private int networkId;
	@JsonProperty("mno_id")
	private int mnoId;
	private int enabled;
    @JsonProperty("api_enabled")
    private boolean apiEnabled;
    @JsonProperty("app_token")
    private String appToken;
    @JsonProperty("app_token_rotated_at")
    private String appTokenRotatedAt;
    @JsonProperty("home_routing")
    private boolean homeRouting;
    @JsonProperty("hss_update_enabled")
    private boolean hssUpdateEnabled;
    @JsonProperty("allowed_traffic")
    private boolean allowedTraffic;
    @JsonProperty("allowed_ussi")
    private boolean allowedUssi;

	@JsonProperty("messages_per_second_high")
	private int messagesPerSecondHigh;

	@JsonProperty("messages_per_second_medium")
	private int messagesPerSecondMedium;

	@JsonProperty("messages_per_second_low")
	private int messagesPerSecondLow;

	@JsonProperty("messages_per_second")
	private int messagesPerSecond;

	@JsonProperty("sip_network_id")
	private Integer sipNetworkId;

	private RedisM3ua m3ua;
	private RedisSccp sccp;
	private TcapDTO tcap;
	private MapDTO map;

    @JsonProperty("settings_home_routing")
    private HomeRoutingDTO settingsHomeRouting;

	@Getter @Setter
	public class RedisM3ua {
		private M3uaDTO general;
		private Redisassociations associations;
		@JsonProperty("application_servers")
		private List<M3uaApplicationServerDTO> applicationServers;
		private List<M3uaRoutesDTO> routes;
	}
	
	@Getter @Setter
	public class Redisassociations {
		private List<M3uaSocketsDTO> sockets;
	    private List<M3uaAssociationsDTO> associations;
	}

	@Getter @Setter
	public class RedisSccp {
		private SccpDTO general;
		@JsonProperty("remote_resources")
		private List<SccpRemoteResourcesDTO> remoteResources;
		@JsonProperty("service_access_points")
		private RedisSap serviceAccessPoints;
		private List<SccpAddressesDTO> addresses;
		private List<SccpRulesDTO> rules;
	}
	
	@Getter @Setter
	public class RedisSap {
		@JsonProperty("service_access")
		private List<SccpServiceAccessPointsDTO> serviceAccess;
		@JsonProperty("mtp3_destinations")
		private List<SccpMtp3DestinationsDTO> mtp3Destinations;
		@JsonProperty("long_message_rules")
		private List<SccpLongMessageRulesDTO> longMessageRules;
	}
	
	@Override
	public String toString() {
		return StaticMethods.toJson(this);
	}
}
