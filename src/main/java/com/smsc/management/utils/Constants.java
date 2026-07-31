package com.smsc.management.utils;

import com.paicbd.smsc.utils.Generated;

import java.util.List;
import java.util.Map;

@Generated
public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final int DISABLED_ENABLED_STATUS = 0;
    public static final int ACTIVE_ENABLED_STATUS = 1;
    public static final int DELETED_ENABLED_STATUS = 2;

    public static final String DEFAULT_STATUS = "STOPPED";
    public static final String STARTED_STATUS = "STARTED";
    public static final String DELETED_STATUS = "DELETED";
    public static final String FORCED_STOPPED_STATUS = "FORCE_STOPPED";
    public static final String REGEX_FOR_VIOLATES_CONSTRAINT = "(ERROR: duplicate key value violates unique constraint \".*?\"\\s+Detail: Key \\(.*?\\)=\\(.*?\\) already exists\\.)";

    // endpoint smpp gateway
    public static final String UPDATE_GATEWAY_ENDPOINT = "/app/updateGateway";
    public static final String CONNECT_GATEWAY_ENDPOINT = "/app/connectGateway";
    public static final String STOP_GATEWAY_ENDPOINT = "/app/stopGateway";
    public static final String DELETE_GATEWAY_ENDPOINT = "/app/deleteGateway";

    // endpoint to http gateway
    public static final String UPDATE_HTTP_GATEWAY_ENDPOINT = "/app/http/updateGateway";
    public static final String CONNECT_HTTP_GATEWAY_ENDPOINT = "/app/http/connectGateway";
    public static final String STOP_HTTP_GATEWAY_ENDPOINT = "/app/http/stopGateway";
    public static final String DELETE_HTTP_GATEWAY_ENDPOINT = "/app/http/deleteGateway";

    public static final String UPDATE_SS7_GATEWAY_ENDPOINT = "/app/ss7/updateGateway";
    public static final String CONNECT_SS7_GATEWAY_ENDPOINT = "/app/ss7/connectGateway";
    public static final String STOP_SS7_GATEWAY_ENDPOINT = "/app/ss7/stopGateway";
    public static final String DELETE_SS7_GATEWAY_ENDPOINT = "/app/ss7/deleteGateway";
    public static final String START_SS7_ASSOCIATION_ENDPOINT = "/app/ss7/startAssociation";
    public static final String STOP_SS7_ASSOCIATION_ENDPOINT = "/app/ss7/stopAssociation";
    public static final String START_SS7_SOCKET_ENDPOINT = "/app/ss7/startSocket";
    public static final String STOP_SS7_SOCKET_ENDPOINT = "/app/ss7/stopSocket";
    public static final String SS7_HOT_RELOAD_ENDPOINT = "/app/ss7/hotReload";
    public static final String SS7_SETTINGS_UPDATE_ENDPOINT = "/app/ss7/settings/update";
    public static final boolean SS7_ALLOWED_TRAFFIC = true;

    public static final String UPDATE_ERROR_CODE_MAPPING_ENDPOINT = "/app/updateErrorCodeMapping"; // Receive mno_id as String
    public static final String DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT = "/app/smpp/serviceProviderDeleted";
    public static final String DELETE_SERVICE_HTTP_PROVIDER_ENDPOINT = "/app/http/serviceProviderDeleted";
    public static final String UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT = "/app/smpp/updateServiceProvider";
    public static final String UPDATE_SERVICE_HTTP_PROVIDER_ENDPOINT = "/app/http/updateServiceProvider";
    public static final String UPDATE_HTTP_SERVER_HANDLER_ENDPOINT = "/app/httpUpdateServerHandler";

    /*
     * routing rules
     */
    public static final String UPDATE_ROUTING_RULES_ENDPOINT = "/app/update/routingRules";
    public static final String DELETE_ROUTING_RULES_ENDPOINT = "/app/delete/routingRules";

    /*
     * general settings
     */
    public static final String GENERAL_SETTINGS_SMPP_HTTP_ENDPOINT = "/app/generalSettings";
    public static final String GENERAL_SMSC_RETRY_ENDPOINT = "/app/generalSmsRetry";
    public static final String GENERAL_SETTINGS_DIAMETER_ENDPOINT = "/app/generalSettings/diameter";
    public static final String DELETE_GENERAL_SETTINGS_DIAMETER_ENDPOINT = "/app/generalSettings/delete/diameter";

    /*
     * smpp-server listener
     */
    public static final String DEFAULT_SMPP_SERVER_NAME = "Default smpp server";
    public static final String UPDATE_SMPP_SERVER_LISTENER = "/app/smpp-server/listener/update";


    /*
     * common variables
     */
    public static final String LOCAL_CHARGING = "USE_LOCAL_CHARGING";
    public static final String USE_ANALYZE = "USE_ANALYZE";
    public static final String USE_DND_FILTERING = "USE_DND_FILTERING";
    public static final String SMSC_ACCOUNT_SETTINGS = "SMSC_ACCOUNT_SETTINGS";
    public static final String KEY_MAX_PASSWORD_LENGTH = "max_password_length";
    public static final String KEY_MAX_SYSTEM_ID_LENGTH = "max_system_id_length";
    
    // Redis keys
    public static final String COMMON_SETTINGS_KEY = "common_settings";

    /*
     * broadcast
     */
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_START = "START";
    public static final String HEADER_LOGS_FILE = "broadcast_id|message_id|enqueue_date|sent_date|source_address|destination_address|status|message|comment|dest_protocol|dest_network_id|dest_network_type|dest_name\n";
    public static final List<String> INVALID_STATUS_TO_DUPLICATE = List.of("DRAFT", "FAILED", "CREATING", "UPDATING");

    /*
     * Interpreters
     */
    public static final String INTERPRETER_KEY_TEMPLATE = "message|input,message|output,response_message|input,response_message|output,response_dlr|output,dlr|input,dlr|output";

    public static final String INTERPRETER_DEFAULT_PATH = """
            {
              "message|input": "/message",
              "dlr|input": "/dlr"
            }
            """;

    public static final String INTERPRETER_DEFAULT_TEMPLATE = """
            {
              "message|input": {
                "system_id": "{{systemId:STRING}}",
                "source_addr_ton": "{{sourceAddrTon:INT}}",
                "source_addr_npi": "{{sourceAddrNpi:INT}}",
                "source_addr": "{{sourceAddr:STRING}}",
                "dest_addr_ton": "{{destAddrTon:INT}}",
                "dest_addr_npi": "{{destAddrNpi:INT}}",
                "destination_addr": "{{destinationAddr:STRING}}",
                "esm_class": "{{esmClass:INT}}",
                "validity_period": "{{validityPeriod:LONG}}",
                "registered_delivery": "{{registeredDelivery:INT}}",
                "data_coding": "{{dataCoding:INT}}",
                "sm_default_msg_id": "{{smDefaultMsgId:INT}}",
                "short_message": "{{shortMessage:STRING}}",
                "optional_parameters": "{{optionalParameters:LIST}}",
                "custom_parameters": "{{customParams:MAP}}"
              },
              "message|output": {
                "message_id": "{{messageId:STRING}}",
                "source_addr_ton": "{{sourceAddrTon:INT}}",
                "source_addr_npi": "{{sourceAddrNpi:INT}}",
                "source_addr": "{{sourceAddr:STRING}}",
                "dest_addr_ton": "{{destAddrTon:INT}}",
                "dest_addr_npi": "{{destAddrNpi:INT}}",
                "destination_addr": "{{destinationAddr:STRING}}",
                "registered_delivery": "{{registeredDelivery:INT}}",
                "data_coding": "{{dataCoding:INT}}",
                "short_message": "{{shortMessage:STRING}}",
                "optional_parameters": "{{optionalParameters:LIST}}",
                "custom_parameters": "{{customParams:MAP}}"
              },
              "dlr|output": {
                "message_id": "{{parentId:STRING}}",
                "source_addr_ton": "{{sourceAddrTon:INT}}",
                "source_addr_npi": "{{sourceAddrNpi:INT}}",
                "source_addr": "{{sourceAddr:STRING}}",
                "dest_addr_ton": "{{destAddrTon:INT}}",
                "dest_addr_npi": "{{destAddrNpi:INT}}",
                "destination_addr": "{{destinationAddr:STRING}}",
                "esm_class": "{{esmClass:INT}}",
                "data_coding": "{{dataCoding:INT}}",
                "short_message": "{{shortMessage:STRING}}",
                "status": "{{status:STRING}}",
                "error_code": "{{errorCode:STRING}}",
                "optional_parameters": "{{optionalParameters:LIST}}",
                "msg_reference_number": "{{msgReferenceNumber:STRING}}",
                "total_segment": "{{totalSegment:INT}}",
                "segment_sequence": "{{segmentSequence:INT}}"
              },
              "dlr|input": {
                "message_id": "{{deliverSmId:STRING}}",
                "source_addr_ton": "{{sourceAddrTon:INT}}",
                "source_addr_npi": "{{sourceAddrNpi:INT}}",
                "source_addr": "{{sourceAddr:STRING}}",
                "dest_addr_ton": "{{destAddrTon:INT}}",
                "dest_addr_npi": "{{destAddrNpi:INT}}",
                "destination_addr": "{{destinationAddr:STRING}}",
                "data_coding": "{{dataCoding:INT}}",
                "status": "{{status:STRING}}",
                "error_code": "{{errorCode:STRING}}",
                "optional_parameters": "{{optionalParameters:LIST}}"
              },
              "response_message|input": {
                "message_id": "{{messageId:STRING}}"
              },
              "response_message|output": {
                "system_id": "{{systemId:STRING}}",
                "message_id": "{{messageId:STRING}}",
                "message": "{{shortMessage:STRING}}"
              },
              "response_dlr|output": {
                "system_id": "{{systemId:STRING}}",
                "message_id": "{{messageId:STRING}}",
                "message": "{{shortMessage:STRING}}"
              }
            }
            """;

    // analyze
    public static final String TABLE_CDR_NAME = "cdr";
    public static final String TABLE_ALIAS_CDR = "c";
    public static final String START_DATETIME_KEY = "start_datetime";
    public static final String END_DATETIME_KEY = "end_datetime";
    public static final String SEARCH_FILTER = "search_filter";
    public static final String BROADCAST_FILTER_ID = "broadcast_filter_id";
    public static final String BROADCAST_FILTER_USER = "broadcast_filter_user";
    public static final String TOTAL_COUNT_COLUMN = "total_count";
    // key -> column table cdr
    public static final Map<String, String> FILTERS_KEY_MAPPING = Map.ofEntries(
            Map.entry(START_DATETIME_KEY, "c.record_date"),
            Map.entry(END_DATETIME_KEY, "c.record_date"),
            Map.entry("message_type", "c.message_type"),
            Map.entry("status", "c.status"),
            Map.entry("origin_type", "c.origination_type"),
            Map.entry("origin_protocol", "c.origination_protocol"),
            Map.entry("destination_protocol", "c.destination_protocol"),
            Map.entry("destination_type", "c.destination_type"),
            Map.entry("source_addr", "c.addr_src_digits"),
            Map.entry("destination_addr", "c.addr_dst_digits"),
            Map.entry("origin_network", "c.origination_network_id"),
            Map.entry("destination_network", "c.destination_network_id"),
            Map.entry("registered_delivery", "c.registered_delivery"),
            Map.entry(BROADCAST_FILTER_ID, "c.broadcast_id"),
            Map.entry(BROADCAST_FILTER_USER, "c.broadcast_id")
    );
    public static final String AND_CONDITION = " AND ";

    public static final int ENABLED = 1;
    public static final int DISABLED = 0;
    public static final int CHUNK_SIZE = 500;
    //constant message
    public static final String SS7_ERROR_MESSAGE_RESPONSE = "Ss7 Gateways was end with error";
    public static final String SS7_SUCCESS_MESSAGE_RESPONSE = "Get Ss7 gateways request success";

    // Priority levels for message routing
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    //JWT service provider http
    public static final String AUDIENCE_HTTP_SERVER = "HTTP-SERVER";
    public static final String PROTOCOL_HTTP = "HTTP";
    public static final String SUBJECT_SEPARATOR = "|";

    public static final String ERROR_SP_NULL = "serviceProvider must not be null";
    public static final String ERROR_PROTOCOL_HTTP_ONLY = "JWT generation applies only to HTTP service providers";
    public static final String ERROR_SYSTEM_ID_BLANK = "serviceProvider.systemId must not be blank";
    public static final String ERROR_NETWORK_ID_INVALID = "serviceProvider.networkId must be greater than 0";
    public static final String ERROR_EXPIRATION_INVALID = "expirationSeconds must be greater than 0";

    public static final int MIN_SECRET_BYTES = 32;

    public static final String AUTH_TYPE_BEARER = "Bearer";
    public static final String AUTH_TYPE_API_KEY = "Api-key";
    public static final String AUTH_TYPE_BASIC = "Basic";
    public static final String AUTH_TYPE_UNDEFINED = "Undefined";

    public static final String DEFAULT_AUTHORIZATION_HEADER = "Authorization";
    public static final String DEFAULT_API_KEY_HEADER = "X-API-Key";

    public static final String ERROR_SERVICE_PROVIDER_NOT_FOUND = "Service provider not found";
    public static final String ERROR_BEARER_AUTH_NOT_CONFIGURED = "Service provider is not configured with Bearer authentication";
    public static final String ERROR_API_KEY_AUTH_NOT_CONFIGURED = "Service provider is not configured with Api-key authentication";
    public static final String ERROR_BEARER_EXPIRATION_REQUIRED = "Bearer token expiration must be configured and greater than 0";

    //sip settings
    public static final String DELETE_SIP_GATEWAYS_ENDPOINT = "/app/sip/deleteGateway";
    public static final String UPDATE_SIP_GATEWAYS_ENDPOINT = "/app/sip/updateGateway";
    public static final String CONNECT_SIP_GATEWAYS_ENDPOINT = "/app/sip/connectGateway";
    public static final String STOP_SIP_GATEWAYS_ENDPOINT = "/app/sip/stopGateway";
    public static final String DUPLICATE_SIP_GATEWAY_MESSAGE = "There is already an active or disabled SIP gateway with the same ip address, port.";

    // Bearer token blacklist (ScyllaDB)
    public static final long BLACKLIST_TTL_BUFFER_SECONDS = 10L;
}
