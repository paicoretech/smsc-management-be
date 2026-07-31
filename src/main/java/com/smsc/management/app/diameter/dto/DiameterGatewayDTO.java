package com.smsc.management.app.diameter.dto;

import com.paicbd.smsc.dto.diameter.ConnectionType;
import com.smsc.management.app.gateway.validation.MessagePriorityRequired;
import com.smsc.management.utils.StaticMethods;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@MessagePriorityRequired
@Getter
@Setter
public class DiameterGatewayDTO {

    private Integer id;

    @JsonProperty("network_id")
    private Integer networkId;

    private String name;

    @JsonProperty("connection_type")
    @Enumerated(EnumType.STRING)
    private ConnectionType connectionType;

    @NotNull(message = "Type cannot be null")
    @Pattern(regexp = "^(OCS|GATEWAY)$", message = "type must be OCS or GATEWAY")
    private String type;

    @JsonProperty("mno_id")
    private Integer mnoId;

    @JsonProperty("global_title")
    private String globalTitle;

    private String protocol = "DIAMETER";

    @JsonProperty("split_message")
    private boolean splitMessage = false;

    @JsonProperty("messages_per_second_high")
    private int messagesPerSecondHigh;

    @JsonProperty("messages_per_second_medium")
    private int messagesPerSecondMedium;

    @JsonProperty("messages_per_second_low")
    private int messagesPerSecondLow;

    @JsonProperty("messages_per_second")
    private int messagesPerSecond;

    public void setMessagesPerSecond() {
        this.messagesPerSecond = this.messagesPerSecondHigh + this.messagesPerSecondMedium + this.messagesPerSecondLow;
    }

    private boolean started;

    @Valid
    @JsonProperty("local_peer")
    private LocalPeerDTO localPeer;
    private Integer localPeerId;

    @Valid
    @JsonProperty("parameters")
    private ParametersDTO parameters;
    private Integer parametersId;

    @Valid
    @JsonProperty("peers")
    private Set<PeerDTO> peers = new HashSet<>();

    @Valid
    @JsonProperty("realms")
    private Set<RealmDTO> realms = new HashSet<>();

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("hss_update_enabled")
    private boolean hssUpdateEnabled;

    @JsonProperty("allowed_traffic")
    private boolean allowedTraffic;


    @Override
    public String toString() {
        return StaticMethods.toJson(this);
    }
}
