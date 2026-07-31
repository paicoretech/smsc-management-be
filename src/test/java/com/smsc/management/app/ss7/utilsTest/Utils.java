package com.smsc.management.app.ss7.utilsTest;

import com.paicbd.smsc.utils.Converter;
import com.smsc.management.app.ss7.dto.HomeRoutingCcMccMncDTO;
import com.smsc.management.app.ss7.dto.HomeRoutingDTO;
import com.smsc.management.app.ss7.dto.M3uaApplicationServerDTO;
import com.smsc.management.app.ss7.dto.M3uaAssociationsDTO;
import com.smsc.management.app.ss7.dto.M3uaDTO;
import com.smsc.management.app.ss7.dto.M3uaRoutesDTO;
import com.smsc.management.app.ss7.dto.M3uaSocketsDTO;
import com.smsc.management.app.ss7.dto.MapDTO;
import com.smsc.management.app.ss7.dto.SccpAddressesDTO;
import com.smsc.management.app.ss7.dto.SccpDTO;
import com.smsc.management.app.ss7.dto.SccpLongMessageRulesDTO;
import com.smsc.management.app.ss7.dto.SccpMtp3DestinationsDTO;
import com.smsc.management.app.ss7.dto.SccpRemoteResourcesDTO;
import com.smsc.management.app.ss7.dto.SccpRulesDTO;
import com.smsc.management.app.ss7.dto.SccpServiceAccessPointsDTO;
import com.smsc.management.app.ss7.dto.Ss7GatewaysDTO;
import com.smsc.management.app.ss7.dto.TcapDTO;
import com.smsc.management.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Utils {
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

                if ("DELETE".equalsIgnoreCase(method) || "SS7REFRESH".equalsIgnoreCase(method)) {
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

    public static Ss7GatewaysDTO getSs7GatewaysDTO() {
        String ss7GatewaysDTOJsonStr = """
                {
                  "name": "ss7 gw",
                  "enabled": 0,
                  "status": "STOPPED",
                  "protocol": "SS7",
                  "network_id": 1,
                  "mno_id": 1,
                  "global_title": "22220",
                  "global_title_indicator": "GT0010",
                  "translation_type": 0,
                  "smsc_ssn": 8,
                  "hlr_ssn": 6,
                  "msc_ssn": 8,
                  "map_version": 2,
                  "split_message": true,
                  "api_enabled": false,
                  "app_token": null,
                  "hss_update_enabled": false,
                  "allowed_traffic": false,
                  "allowed_ussi": false
                }
            """;

        return Converter.stringToObject(ss7GatewaysDTOJsonStr, Ss7GatewaysDTO.class);
    }

    public static Ss7GatewaysDTO newSs7GatewaysDTO() {
        String ss7GatewaysDTOJsonStr = """
                {
                  "name": "ss7 gw",
                  "status": "CLOSED",
                  "enabled": "0",
                  "mno_id": "1",
                  "global_title": "22220",
                  "global_title_indicator": "GT0010",
                  "translation_type": "0",
                  "smsc_ssn": "8",
                  "hlr_ssn": "6",
                  "msc_ssn": "8",
                  "map_version": "2",
                  "split_message": true,
                  "api_enabled": false,
                  "app_token": null,
                  "hss_update_enabled": false,
                  "allowed_traffic": false,
                  "allowed_ussi": false
                }
            """;

        return Converter.stringToObject(ss7GatewaysDTOJsonStr, Ss7GatewaysDTO.class);
    }

    public static TcapDTO getTcapDTOMock() {
        String tcapJsonStr = """
                {
                "id": 2,
                  "network_id": 3,
                  "ssn_list": "8",
                  "preview_mode": "false",
                  "dialog_idle_timeout": "100000",
                  "invoke_timeout": "25000",
                  "dialog_id_range_start": "1",
                  "dialog_id_range_end": "2147483647",
                  "max_dialogs": "5000",
                  "do_not_send_protocol_version": "false",
                  "swap_tcap_id_enabled": "true",
                  "sls_range_id": "All",
                  "exr_delay_thr1": "1",
                  "exr_delay_thr2": "6",
                  "exr_delay_thr3": "12",
                  "exr_back_to_normal_delay_thr1": "0.5",
                  "exr_back_to_normal_delay_thr2": "3",
                  "exr_back_to_normal_delay_thr3": "8",
                  "memory_monitor_thr1": "77",
                  "memory_monitor_thr2": "87",
                  "memory_monitor_thr3": "97",
                  "mem_back_to_normal_delay_thr1": "72",
                  "mem_back_to_normal_delay_thr2": "82",
                  "mem_back_to_normal_delay_thr3": "92",
                  "blocking_incoming_tcap_messages": "false"
                }""";

        return Converter.stringToObject(tcapJsonStr, TcapDTO.class);
    }

    public static MapDTO getMapDTOMock() {
        String mapJsonStr = """
                {
                    "id": 2,
                    "network_id": 3,
                    "sri_service_op_code": 45,
                    "forward_sm_service_op_code": 44
                  }""";

        return Converter.stringToObject(mapJsonStr, MapDTO.class);
    }

    public static M3uaDTO getM3uatDtoMock() {
        String m3uaJsonStr = """
                {
                  "id": 1,
                  "network_id": 3,
                  "connect_delay": 5000,
                  "max_sequence_number": 256,
                  "max_for_route": 2,
                  "thread_count": 1,
                  "routing_label_format": "ITU",
                  "heart_beat_time": 10000,
                  "routing_key_management_enabled": false,
                  "use_lowest_bit_for_link": false,
                  "cc_delay_threshold_1": 2.6,
                  "cc_delay_threshold_2": 2.5,
                  "cc_delay_threshold_3": 3,
                  "cc_delay_back_to_normal_threshold_1": 3.4,
                  "cc_delay_back_to_normal_threshold_2": 4.5,
                  "cc_delay_back_to_normal_threshold_3": 5
                }""";

        return Converter.stringToObject(m3uaJsonStr, M3uaDTO.class);
    }

    public static M3uaDTO newM3uatDtoMock() {
        String m3uaJsonStr = """
                {
                  "network_id": 5,
                  "connect_delay": "5000",
                  "thread_count": "1",
                  "heart_beat_time": "10000",
                  "routing_key_management_enabled": "true",
                  "cc_delay_threshold_1": "2.6",
                  "cc_delay_threshold_2": "2.5",
                  "cc_delay_threshold_3": "3.0",
                  "cc_delay_back_to_normal_threshold_1": "3.4",
                  "cc_delay_back_to_normal_threshold_2": "4.5",
                  "cc_delay_back_to_normal_threshold_3": "5.0"
                }
            """;

        return Converter.stringToObject(m3uaJsonStr, M3uaDTO.class);
    }

    public static M3uaSocketsDTO getM3uaSocketsMock() {
        String m3uaSocketsJsonStr = """
                {
                  "id": 1,
                  "name": "sock1",
                  "state": "STOPPED",
                  "enabled": 0,
                  "socket_type": "Client",
                  "transport_type": "TCP",
                  "host_address": "10.11.10.151",
                  "host_port": 2806,
                  "extra_address": "10.11.10.152",
                  "max_concurrent_connections": 1334,
                  "ss7_m3ua_id": 1
                }
            """;

        return Converter.stringToObject(m3uaSocketsJsonStr, M3uaSocketsDTO.class);
    }

    public static M3uaSocketsDTO newM3uaSocketsMock() {
        String  m3uaSocketsJsonStr = """
                {
                  "name": "sock1",
                  "transport_type": "TCP",
                  "max_concurrent_connections": "1334",
                  "host_address": "10.11.10.151",
                  "host_port": "2806",
                  "extra_address": "10.11.10.152",
                  "socket_type": "Client",
                  "ss7_m3ua_id": 1
                }
            """;

        return Converter.stringToObject(m3uaSocketsJsonStr, M3uaSocketsDTO.class);
    }

    public static M3uaAssociationsDTO getM3uaAssociationsDTO() {
        String m3uaAssociationsJsonStr = """
                {
                  "id": 1,
                  "name": "ASOC1",
                  "state": "STOPPED",
                  "peer": "192.168.1.13",
                  "enabled": 0,
                  "peer_port": 2908,
                  "m3ua_heartbeat": true,
                  "m3ua_socket_id": 2,
                  "asp_name": "ASP ASOC1"
                }
        """;

        return Converter.stringToObject(m3uaAssociationsJsonStr, M3uaAssociationsDTO.class);
    }

    public static M3uaAssociationsDTO newM3uaAssociationsDTO() {
        String  m3uaAssociationsJsonStr = """
                {
                   "name": "ASOC1",
                   "m3ua_socket_id": "2",
                   "peer": "192.168.1.13",
                   "peer_port": "2908",
                   "m3ua_heartbeat": "TRUE"
                 }
        """;

        return Converter.stringToObject(m3uaAssociationsJsonStr, M3uaAssociationsDTO.class);
    }

    public static M3uaApplicationServerDTO getM3uaApplicationServer() {
        String  m3uaApplicationServerJsonStr = """
                {
                  "id": 1,
                  "name": "app1",
                  "state": "STARTED",
                  "functionality": "SGW",
                  "exchange": "SE",
                  "routing_context": 101,
                  "network_appearance": 102,
                  "traffic_mode_id": 1,
                  "minimum_asp_for_loadshare": 10
                }
              """;

        M3uaApplicationServerDTO m3uaApplicationServerDTO =  Converter.stringToObject(m3uaApplicationServerJsonStr, M3uaApplicationServerDTO.class);
        m3uaApplicationServerDTO.setAspFactories(new ArrayList<>(Arrays.asList(1, 2)));

        return m3uaApplicationServerDTO;
    }

    public static M3uaApplicationServerDTO newM3uaApplicationServer() {
        String  m3uaApplicationServerJsonStr = """
                {
                 "name": "app1",
                 "functionality": "SGW",
                 "exchange": "SE",
                 "routing_context": 101,
                 "network_appearance": 102,
                 "traffic_mode_id": "1",
                 "minimum_asp_for_loadshare": "0"
               }
              """;

        M3uaApplicationServerDTO m3uaApplicationServerDTO =  Converter.stringToObject(m3uaApplicationServerJsonStr, M3uaApplicationServerDTO.class);
        m3uaApplicationServerDTO.setAspFactories(new ArrayList<>(Arrays.asList(1, 2)));

        return m3uaApplicationServerDTO;
    }

    public static M3uaRoutesDTO getM3uaRoutesDTO() {
        String m3uaRoutesJsonStr = """
                {
                  "id": 1,
                  "origination_point_code": 100,
                  "destination_point_code": 200,
                  "service_indicator": 3,
                  "traffic_mode_id": 1,
                  "app_servers": [
                    4
                  ]
                }
            """;

        return Converter.stringToObject(m3uaRoutesJsonStr, M3uaRoutesDTO.class);
    }

    public static M3uaRoutesDTO newM3uaRoutesDTO() {
        String m3uaRoutesJsonStr = """
                {
                   "origination_point_code": 100,
                   "destination_point_code": 200,
                   "service_indicator": 3,
                   "traffic_mode_id": "1",
                   "app_servers": [
                     4
                   ]
                 }
            """;

        return Converter.stringToObject(m3uaRoutesJsonStr, M3uaRoutesDTO.class);
    }

    public static SccpDTO getSccpDTOMock() {
        String sccpDTOJsonStr = """
                {
                    "id": 1,
                    "network_id": 5,
                    "z_margin_xudt_message": 240,
                    "remove_spc": true,
                    "sst_timer_duration_min": 10000,
                    "sst_timer_duration_max": 6000000,
                    "sst_timer_duration_increase_factor": 1,
                    "max_data_message": 2560,
                    "period_of_logging": 60000,
                    "reassembly_timer_delay": 15000,
                    "preview_mode": false,
                    "sccp_protocol_version": "ANSI",
                    "congestion_control_timer_a": 400,
                    "congestion_control_timer_d": 2000,
                    "congestion_control_algorithm": "international",
                    "congestion_control": false
                  }
            """;

        return Converter.stringToObject(sccpDTOJsonStr, SccpDTO.class);
    }

    public static SccpDTO newSccpDTOMock() {
        String sccpDTOJsonStr = """
                {
                  "network_id": 5,
                  "z_margin_xudt_message": 240,
                  "remove_spc": "true",
                  "sst_timer_duration_min": 10000,
                  "sst_timer_duration_max": 6000000,
                  "sst_timer_duration_increase_factor": 1,
                  "max_data_message": 2560,
                  "period_of_logging": 60000,
                  "reassembly_timer_delay": 15000,
                  "preview_mode": "false",
                  "sccp_protocol_version": "ITU",
                  "congestion_control_timer_a": 400,
                  "congestion_control_timer_d": 2000,
                  "congestion_control_algorithm": "international",
                  "congestion_control": "false"
                }
            """;

        return Converter.stringToObject(sccpDTOJsonStr, SccpDTO.class);
    }

    public static SccpRemoteResourcesDTO getSccpRemoteResourcesDTOMock() {
        String sccpRemoteResourcesDTOJsonStr = """
                {
                  "id": 1,
                  "remote_spc": 200,
                  "remote_spc_status": "ALLOWED",
                  "remote_sccp_status": "ALLOWED",
                  "remote_ssn": 8,
                  "remote_ssn_status": "ALLOWED",
                  "mark_prohibited": true,
                  "ss7_sccp_id": 1
                }
            """;

        return Converter.stringToObject(sccpRemoteResourcesDTOJsonStr, SccpRemoteResourcesDTO.class);
    }

    public static SccpRemoteResourcesDTO newSccpRemoteResourcesDTOMock() {
        String sccpRemoteResourcesDTOJsonStr = """
                {
                  "ss7_sccp_id": 1,
                  "remote_spc": 200,
                  "remote_ssn": 8,
                  "mark_prohibited": "true",
                  "remote_spc_status": "ALLOWED",
                  "remote_ssn_status": "ALLOWED",
                  "remote_sccp_status": "ALLOWED"
                }
            """;

        return Converter.stringToObject(sccpRemoteResourcesDTOJsonStr, SccpRemoteResourcesDTO.class);
    }

    public static SccpServiceAccessPointsDTO getSccpServiceAccessPointsDTOMock() {
        String sccpServiceAccessPointsDTOJsonStr = """
                {
                  "id": 1,
                  "name": "sap1",
                  "origin_point_code": 100,
                  "network_indicator": 2,
                  "local_gt_digits": "*",
                  "ss7_sccp_id": 1
                }
            """;

        return Converter.stringToObject(sccpServiceAccessPointsDTOJsonStr, SccpServiceAccessPointsDTO.class);
    }

    public static SccpServiceAccessPointsDTO newSccpServiceAccessPointsDTOMock() {
        String sccpServiceAccessPointsDTOJsonStr = """
                {
                  "ss7_sccp_id": 1,
                  "name": "sap1",
                  "origin_point_code": "100",
                  "network_indicator": 2,
                  "local_gt_digits": "*"
                }
            """;

        return Converter.stringToObject(sccpServiceAccessPointsDTOJsonStr, SccpServiceAccessPointsDTO.class);
    }

    public static SccpMtp3DestinationsDTO getSccpMtp3DestinationsDTOMock() {
        String sccpMtp3DestinationsDTOJsonStr = """
                {
                  "id": 1,
                  "name": "mtp3 destination",
                  "first_point_code": 200,
                  "last_point_code": 200,
                  "first_sls": 0,
                  "last_sls": 255,
                  "sls_mask": 255,
                  "sccp_sap_id": 1
                }
            """;

        return Converter.stringToObject(sccpMtp3DestinationsDTOJsonStr, SccpMtp3DestinationsDTO.class);
    }

    public static SccpMtp3DestinationsDTO newSccpMtp3DestinationsDTOMock() {
        String sccpMtp3DestinationsDTOJsonStr = """
                {
                  "name": "mtp3Dest1",
                  "first_point_code": "200",
                  "last_point_code": "200",
                  "first_sls": "0",
                  "last_sls": "255",
                  "sls_mask": "255",
                  "sccp_sap_id": "1"
                }
            """;

        return Converter.stringToObject(sccpMtp3DestinationsDTOJsonStr, SccpMtp3DestinationsDTO.class);
    }

    public static SccpAddressesDTO getSccpAddressesDTOMock() {
        String sccpAddressesDTOJsonStr = """
                {
                  "id": 1,
                  "name": "addr1",
                  "digits": "*",
                  "address_indicator": 0,
                  "point_code": 0,
                  "subsystem_number": 0,
                  "gt_indicator": "GT0000",
                  "translation_type": 0,
                  "numbering_plan_id": -1,
                  "nature_of_address_id": -1,
                  "ss7_sccp_id": 1
                }
            """;

        return Converter.stringToObject(sccpAddressesDTOJsonStr, SccpAddressesDTO.class);
    }

    public static SccpAddressesDTO newSccpAddressesDTOMock() {
        String sccpAddressesDTOJsonStr = """
                {
                  "ss7_sccp_id": 1,
                  "name": "addr1",
                  "address_indicator": 0,
                  "point_code": 0,
                  "subsystem_number": 0,
                  "translation_type": 0,
                  "numbering_plan_id": -1,
                  "nature_of_address_id": -1,
                  "digits": "*"
                }
            """;

        return Converter.stringToObject(sccpAddressesDTOJsonStr, SccpAddressesDTO.class);
    }

    public static SccpRulesDTO getSccpRulesDTOMock() {
        String sccpRulesDTOJsonStr = """
                {
                  "id": 1,
                  "name": "rule1",
                  "mask": "K",
                  "address_indicator": 17,
                  "point_code": 1,
                  "subsystem_number": 0,
                  "gt_indicator": "GT0100",
                  "translation_type": 1,
                  "numbering_plan_id": 0,
                  "nature_of_address_id": 0,
                  "global_tittle_digits": "*",
                  "rule_type_id": 1,
                  "primary_address_id": 2,
                  "secondary_address_id": null,
                  "load_sharing_algorithm_id": 2,
                  "origination_type_id": 2,
                  "new_calling_party_address": null,
                  "calling_address_indicator": 0,
                  "calling_point_code": 0,
                  "calling_subsystem_number": 0,
                  "calling_translator_type": 0,
                  "calling_numbering_plan_id": 1,
                  "calling_nature_of_address_id": 6,
                  "calling_gt_indicator": "GT0100",
                  "calling_global_tittle_digits": ""
                }
            """;

        return Converter.stringToObject(sccpRulesDTOJsonStr, SccpRulesDTO.class);
    }

    public static SccpRulesDTO newSccpRulesDTOMock() {
        String sccpRulesDTOJsonStr = """
                {
                  "name": "rule1",
                  "mask": "K",
                  "address_indicator": 17,
                  "point_code": 1,
                  "subsystem_number": 0,
                  "translation_type": 1,
                  "numbering_plan_id": 0,
                  "nature_of_address_id": 0,
                  "global_tittle_digits": "*",
                  "rule_type_id": 1,
                  "primary_address_id": 2,
                  "secondary_address_id": null,
                  "load_sharing_algorithm_id": 2,
                  "new_calling_party_address": null,
                  "origination_type_id": 2,
                  "calling_address_indicator": 0,
                  "calling_point_code": 0,
                  "calling_subsystem_number": 0,
                  "calling_translator_type": 0,
                  "calling_numbering_plan_id": 1,
                  "calling_nature_of_address_id": 6,
                  "calling_global_tittle_digits": ""
                }
            """;

        return Converter.stringToObject(sccpRulesDTOJsonStr, SccpRulesDTO.class);
    }

    public static SccpLongMessageRulesDTO getSccpLongMessageRulesDTOMock() {
        return new SccpLongMessageRulesDTO(1, 100, 200, "XUDT_ENABLED", 1);
    }

    public static SccpLongMessageRulesDTO newSccpLongMessageRulesDTOMock() {
        return new SccpLongMessageRulesDTO(0, 100, 200, "LUDT_ENABLED_WITH_SEGMENTATION", 1);
    }

    public static HomeRoutingDTO getHomeRoutingDTOMock() {
        String homeRoutingDTOJsonStr = """
                {
                    "id": "1",
                    "network_id": "3",
                    "external_id": "",
                    "mode": "TRANSPARENT",
                    "ttl_cache": "300"
                }""";

        return Converter.stringToObject(homeRoutingDTOJsonStr, HomeRoutingDTO.class);
    }

    public static HomeRoutingDTO newHomeRoutingDTOMock() {
        String m3uaJsonStr = """
                {
                    "networkId": "3",
                    "external_id": "",
                    "mode": "TRANSPARENT",
                    "ttl_cache": "350"
                }
            """;

        return Converter.stringToObject(m3uaJsonStr, HomeRoutingDTO.class);
    }

    public static HomeRoutingCcMccMncDTO getHomeRoutingCcMccMncDTOMock() {
        String homeRoutingCcMccMncDTOJsonStr = """
            {
                "id": "1",
                "country_code": "505",
                "mcc_mnc": "71030",
                "smsc": "smsc-test",
                "ss7_home_routing_id": 3
            }""";

        return Converter.stringToObject(homeRoutingCcMccMncDTOJsonStr, HomeRoutingCcMccMncDTO.class);
    }

    public static HomeRoutingCcMccMncDTO newHomeRoutingCcMccMncDTOMock() {
        String homeRoutingCcMccMncDTOJsonStr = """
            {
                "country_code": "505",
                "mcc_mnc": "71031",
                "smsc": "smsc-new",
                "ss7_home_routing_id": 4
            }""";

        return Converter.stringToObject(homeRoutingCcMccMncDTOJsonStr, HomeRoutingCcMccMncDTO.class);
    }
}
