package com.smsc.management.app.sip.utilsTest;

import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.sip.dto.SipGatewaysDTO;
import com.smsc.management.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SipUtils {

    public static void checkAssertions(ResponseEntity<ApiResponse> response, HttpStatus httpStatus) {
        checkAssertions(response, httpStatus, "");
    }

    public static void checkAssertions(ResponseEntity<ApiResponse> response, HttpStatus httpStatus, String method) {
        assertNotNull(response);
        assertInstanceOf(ApiResponse.class, response.getBody());
        ApiResponse apiResponse = response.getBody();

        switch (httpStatus) {
            case OK -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("success", response.getBody().message());

                if ("DELETE".equalsIgnoreCase(method) || "SIPREFRESH".equalsIgnoreCase(method)) {
                    assertNull(Objects.requireNonNull(apiResponse).data());
                } else {
                    assertNotNull(Objects.requireNonNull(apiResponse).data());
                }
            }
            case NOT_FOUND -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case BAD_REQUEST -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertEquals("error", response.getBody().message());
            }
            case INTERNAL_SERVER_ERROR -> {
                assertNull(Objects.requireNonNull(apiResponse).data());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNull(response.getBody().data());
                assertEquals("error", response.getBody().message());
            }
            default -> throw new IllegalStateException("Unexpected value: " + response.getStatusCode());
        }
    }

    public static SipGatewaysDTO getSipGatewaysDTO() {
        String json = """
            {
              "name": "sip gw",
              "mno_id":1,
              "enabled": 0,
              "status": "STOPPED",
              "protocol": "SIP",
              "network_id": 1,

              "ip_address": "10.0.0.1",
              "port": 5060,
              "transport": "UDP",
              "messages_per_second_high": 70,
              "messages_per_second_medium": 20,
              "messages_per_second_low": 10,
              "messages_per_second": 100,

              "transaction_timeout": 32000,
              "retransmission_base_interval_ms": 500,
              "retransmission_max_interval_ms": 4000,
              "network_timeout_ms": 10000,

              "thread_pool_size": 8,
              "retransmission_filter": true,
              "max_message_size": 4096,

              "routing_enable_ss7": true,
              "routing_enable_diameter": false,
              "routing_registration_traffic_ss7_gateway_id": 10,
              "routing_registration_traffic_diameter_gateway_id": null,
              "routing_ussi_traffic_ss7_gateway_id": 99,
              "external_id": 2,
              "protocol": "SIP",
              "status": "STARTED",
              "register_max_expires": 3600,
              "subscribe_target_host": "1.2.3.4",
              "subscribe_target_port": 5060,
              "subscribe_target_transport": "UDP",
              "local_via_host": "10.0.0.1",
              "auto_retry_error_code": "408,480,500,503",
              "no_retry_error_code": "400,403,404",
              "retry_alternate_destination_error_code": "408,503",
              "global_title": "12345",
              "ussi_default_datacoding_id": 1
            }
        """;

        return Converter.stringToObject(json, SipGatewaysDTO.class);
    }

    public static SipGatewaysDTO newSipGatewaysDTO() {
        String json = """
            {
              "name": "sip gw",
              "mno_id":1,
              "enabled": "0",
              "status": "CLOSED",
              "protocol": "SIP",
              "network_id": 1,

              "ip_address": "10.0.0.1",
              "port": "5060",
              "transport": "UDP",
              "messages_per_second_high": "70",
              "messages_per_second_medium": "20",
              "messages_per_second_low": "10",
              "messages_per_second": "100",

              "transaction_timeout": "32000",
              "retransmission_base_interval_ms": "500",
              "retransmission_max_interval_ms": "4000",
              "network_timeout_ms": "10000",

              "thread_pool_size": "8",
              "retransmission_filter": "true",
              "max_message_size": "4096",


              "routing_enable_ss7": "true",
              "routing_enable_diameter": "false",
              "routing_registration_traffic_ss7_gateway_id": "10",
              "routing_registration_traffic_diameter_gateway_id": null,
              "routing_ussi_traffic_ss7_gateway_id": "99",
              "external_id": 2,
              "protocol": "SIP",
              "status": "STARTED",
              "register_max_expires": 3600,
              "subscribe_target_host": "1.2.3.4",
              "subscribe_target_port": 5060,
              "subscribe_target_transport": "UDP",
              "local_via_host": "10.0.0.1",
              "auto_retry_error_code": "408,480,500,503",
              "no_retry_error_code": "400,403,404",
              "retry_alternate_destination_error_code": "408,503",
              "global_title": "12345",
              "ussi_default_datacoding_id": 1
            }
        """;

        return Converter.stringToObject(json, SipGatewaysDTO.class);
    }
}
