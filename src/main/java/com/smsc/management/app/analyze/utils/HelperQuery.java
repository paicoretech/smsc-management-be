package com.smsc.management.app.analyze.utils;

import com.paicbd.smsc.utils.Generated;

import static com.smsc.management.utils.Constants.TABLE_ALIAS_CDR;
import static com.smsc.management.utils.Constants.TABLE_CDR_NAME;

@Generated
public class HelperQuery {
    private HelperQuery() {
        throw new IllegalStateException("Utility class");
    }

    public static final String FORMAT_TABLE_NAME = "reports.data_cdr_report_file_id_%s";

    public static final String NETWORK_QUERY = """
        SELECT CAST(g.network_id AS VARCHAR) AS network_id, g.name
                    FROM gateways g
                    UNION ALL
                    SELECT CAST(g.network_id AS VARCHAR), g.name
                    FROM ss7_gateways g
                    UNION ALL
                    SELECT CAST(g.network_id AS VARCHAR), g.name
                    FROM service_provider g
                    UNION ALL
                    SELECT CAST(dg.network_id AS VARCHAR), dg.name
                    FROM diameter_gateway dg
    """;

    public static final String DATA_REPORT_CDRS_QUERY = """
                CREATE SCHEMA IF NOT EXISTS reports;
                CREATE TABLE %s AS
                WITH network_name AS (
                    %s
                )
                SELECT c.id,
                       c.record_date,
                       c.submit_date,
                       c.delivery_date,
                       c.message_type,
                       c.message_id,
                       c.origination_protocol,
                       c.origination_type,
                       (SELECT name FROM network_name WHERE network_id = c.origination_network_id) as origination_network,
                       c.destination_protocol,
                       c.destination_type,
                       (SELECT name FROM network_name WHERE network_id = c.destination_network_id) as destination_network,
                       c.routing_id,
                       c.status,
                       c.status_code,
                       c.comment,
                       c.dialog_duration,
                       c.processing_time,
                       c.data_coding,
                       c.validity_period,
                       c.addr_src_digits as sender,
                       c.addr_dst_ton,
                       c.addr_dst_npi,
                       c.addr_dst_digits as destination,
                       c.remote_dialog_id,
                       c.local_dialog_id,
                       c.local_spc,
                       c.local_ssn,
                       c.local_global_title_digits,
                       c.remote_spc,
                       c.remote_ssn,
                       c.remote_global_title_digits,
                       c.imsi,
                       c.nnn_digits,
                       c.originator_sccp_address,
                       c.mt_service_center_address,
                       c.message,
                       c.esm_class,
                       c.udhi,
                       c.registered_delivery,
                       c.msg_reference_number,
                       c.total_segment,
                       c.segment_sequence,
                       c.retry_number,
                       c.parent_id,
                       c.broadcast_id,
                       b.name as broadcast_name,
                       u.user_name
                FROM cdr c
                LEFT JOIN broadcast b ON c.broadcast_id = b.id
                LEFT JOIN users u ON b.created_by_id = u.id
                %s
                """;

    public static final String PAGINATION_QUERY_REPORT_CDR = """
                SELECT record_date,
                       submit_date,
                       delivery_date,
                       message_type,
                       message_id,
                       origination_protocol,
                       origination_type,
                       origination_network,
                       destination_protocol,
                       destination_type,
                       destination_network,
                       routing_id,
                       status,
                       status_code,
                       comment,
                       dialog_duration,
                       processing_time,
                       data_coding,
                       validity_period,
                       sender,
                       addr_dst_ton,
                       addr_dst_npi,
                       destination,
                       remote_dialog_id,
                       local_dialog_id,
                       local_spc,
                       local_ssn,
                       local_global_title_digits,
                       remote_spc,
                       remote_ssn,
                       remote_global_title_digits,
                       imsi,
                       nnn_digits,
                       originator_sccp_address,
                       mt_service_center_address,
                       message,
                       esm_class,
                       udhi,
                       registered_delivery,
                       msg_reference_number,
                       total_segment,
                       segment_sequence,
                       retry_number,
                       parent_id,
                       broadcast_id,
                       broadcast_name,
                       user_name
                FROM %s
                ORDER BY id
                """;

    public static final String DATA_REPORT_CDRS_DETAILED_QUERY = """
            CREATE SCHEMA IF NOT EXISTS reports;
            CREATE TABLE %s AS
            WITH network_name AS (
                %s
            )
            SELECT
                c.id,
                nn1.name AS Account_Name,
                c.addr_src_digits AS Sender,
                c.addr_dst_digits AS destination,
                c.message_id AS Message_id,
                c.submit_date AS Send_At,
                nn2.name AS Network_Name,
                c.message_type AS Message_Type,
                c.status,
                CASE
                    WHEN c.status = 'SUCCESS' THEN 'no error (code 0)'
                ELSE lower(sc.status)
                END AS Error_Name,
                c.delivery_date AS Done_AT,
                c.message AS Text,
                length(c.message) AS Message_Length
            FROM cdr c
                     INNER JOIN network_name nn1 ON nn1.network_id = c.origination_network_id
                     INNER JOIN network_name nn2 ON nn2.network_id = c.destination_network_id
                     LEFT JOIN broadcast b ON c.broadcast_id = b.id
                     LEFT JOIN users u ON b.created_by_id = u.id
                     LEFT JOIN cdr_status_code sc ON (c.status_code = sc.status_code AND c.origination_protocol = sc.protocol)
            %s
            """;

    public static final String PAGINATION_QUERY_REPORT_DETAILED = """
            SELECT
                Account_Name,
                Sender,
                destination,
                Message_id,
                Send_At,
                Network_Name,
                Message_Type,
                status,
                CASE
                    WHEN status = 'SUCCESS' THEN 'no error (code 0)'
                ELSE lower(status)
                END AS Error_Name,
                Done_AT,
                Text,
                Message_Length
            FROM %s
            ORDER BY id
            """;
    public static final String REPORT_ACCOUNT_QUERY = """
            WITH network_name AS (
                    %s
                ),
                cdr_with_status AS (
                     SELECT
                         c.*,
                         cs.status AS mapped_status
                     FROM cdr c
                              LEFT JOIN cdr_status_code cs
                                        ON (c.status_code = cs.status_code
                                            AND c.origination_protocol = cs.protocol)
                     %s
                ),
                summary_cdr AS (
                    SELECT
                        nn.name AS Account_Name,
                        COUNT(*) FILTER (WHERE cws.status = 'SUCCESS') AS Delivered,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND cws.mapped_status = 'REJECTED') AS Rejected,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND (cws.mapped_status = 'UNDELIVERED' OR cws.mapped_status is null)) AS Undeliverable,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND cws.mapped_status = 'EXPIRED') AS Expired,
                        COUNT(*) AS Total_Messages
                    FROM cdr_with_status cws
                             INNER JOIN network_name nn ON (cws.origination_network_id = nn.network_id)
                    GROUP BY nn.name
                    ORDER BY nn.name
                )
                SELECT Account_Name,
                    Delivered, Rejected,
                    Undeliverable,
                    Expired,
                    (Delivered * 100) / NULLIF(Total_Messages, 0) AS Delivery_Rate,
                    Total_Messages,
                    CAST(SUM(Total_Messages) OVER() AS BIGINT) AS total_count
                FROM summary_cdr
            """;

    public static final String REPORT_ACCOUNT_TRAFFIC_QUERY = """
            WITH network_name AS (
                    %s
                ),
                cdr_with_status AS (
                     SELECT
                         c.*,
                         cs.status AS mapped_status
                     FROM cdr c
                              LEFT JOIN cdr_status_code cs
                                        ON (c.status_code = cs.status_code
                                            AND c.origination_protocol = cs.protocol)
                     %s
                ),
                summary_cdr AS (
                    SELECT
                        nn.name AS Account_Name,
                        COUNT(*) AS Total_Messages
                    FROM cdr_with_status cws
                             INNER JOIN network_name nn ON (cws.origination_network_id = nn.network_id)
                    GROUP BY nn.name
                    ORDER BY nn.name
                )
                SELECT Account_Name,
                    Total_Messages,
                    CAST(SUM(Total_Messages) OVER() AS BIGINT) AS total_count
                FROM summary_cdr
            """;

    public static final String REPORT_BROADCAST_QUERY = """
            WITH cdr_with_status AS (
                     SELECT
                         c.*,
                         cs.status AS mapped_status
                     FROM cdr c
                              LEFT JOIN cdr_status_code cs
                                        ON (c.status_code = cs.status_code
                                            AND c.origination_protocol = cs.protocol)
                     %s
                ),
                summary_cdr AS (
                    SELECT
                        b.id as Broadcast_Id,
                        b.name AS Broadcast_Name,
                        COUNT(*) FILTER (WHERE cws.status = 'SUCCESS') AS Delivered,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND cws.mapped_status = 'REJECTED') AS Rejected,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND (cws.mapped_status = 'UNDELIVERED' OR cws.mapped_status is null)) AS Undeliverable,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND cws.mapped_status = 'EXPIRED') AS Expired,
                        COUNT(*) AS Total_Messages
                    FROM cdr_with_status cws
                             INNER JOIN broadcast b ON (cws.broadcast_id = b.id)
                    GROUP BY b.id, b.name
                    ORDER BY b.id
                )
                SELECT Broadcast_Id,
                    Broadcast_Name,
                    Delivered,
                    Rejected,
                    Undeliverable,
                    Expired,
                    (Delivered * 100) / NULLIF(Total_Messages, 0) AS Delivery_Rate,
                    Total_Messages,
                    CAST(SUM(Total_Messages) OVER() AS BIGINT) AS total_count
                FROM summary_cdr
            """;

    public static final String REPORT_ACCOUNT_DESTINATION_QUERY = """
            WITH network_name AS (
                    %s
                ),
                cdr_with_status AS (
                     SELECT
                         c.*,
                         cs.status AS mapped_status
                     FROM cdr c
                              LEFT JOIN cdr_status_code cs
                                        ON (c.status_code = cs.status_code
                                            AND c.origination_protocol = cs.protocol)
                     %s
                ),
                summary_cdr AS (
                    SELECT
                        nn.name AS Account_Name,
                        COUNT(*) FILTER (WHERE cws.status = 'SUCCESS') AS Delivered,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND cws.mapped_status = 'REJECTED') AS Rejected,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND (cws.mapped_status = 'UNDELIVERED' OR cws.mapped_status is null)) AS Undeliverable,
                        COUNT(*) FILTER (WHERE cws.status = 'FAILED' AND cws.mapped_status = 'EXPIRED') AS Expired,
                        COUNT(*) AS Total_Messages
                    FROM cdr_with_status cws
                             INNER JOIN network_name nn ON (cws.destination_network_id = nn.network_id)
                    GROUP BY nn.name
                    ORDER BY nn.name
                )
                SELECT Account_Name,
                    Delivered, Rejected,
                    Undeliverable,
                    Expired,
                    (Delivered * 100) / NULLIF(Total_Messages, 0) AS Delivery_Rate,
                    Total_Messages,
                    CAST(SUM(Total_Messages) OVER() AS BIGINT) AS total_count
                FROM summary_cdr
            """;

    public static final String REPORT_ACCOUNT_DESTINATION_TRAFFIC_QUERY = """
            WITH network_name AS (
                    %s
                ),
                cdr_with_status AS (
                     SELECT
                         c.*,
                         cs.status AS mapped_status
                     FROM cdr c
                              LEFT JOIN cdr_status_code cs
                                        ON (c.status_code = cs.status_code
                                            AND c.origination_protocol = cs.protocol)
                     %s
                ),
                summary_cdr AS (
                    SELECT
                        nn.name AS Account_Name,
                        COUNT(*) AS Total_Messages
                    FROM cdr_with_status cws
                             INNER JOIN network_name nn ON (cws.destination_network_id = nn.network_id)
                    GROUP BY nn.name
                    ORDER BY nn.name
                )
                SELECT Account_Name,
                    Total_Messages,
                    CAST(SUM(Total_Messages) OVER() AS BIGINT) AS total_count
                FROM summary_cdr
            """;

    public static final String REPORT_USER_AND_SENDER_TRAFFIC_QUERY = """
            WITH cdr_with_status AS (
                     SELECT
                         c.*,
                         cs.status AS mapped_status
                     FROM cdr c
                              LEFT JOIN cdr_status_code cs
                                        ON (c.status_code = cs.status_code
                                            AND c.origination_protocol = cs.protocol)
                     %s
                ),
                summary_cdr AS (
                     SELECT
                         u.user_name AS Account,
                         cws.addr_src_digits AS Sender,
                         COUNT(*) AS Total_Messages
                     FROM cdr_with_status cws
                              INNER JOIN broadcast b ON (cws.broadcast_id = b.id)
                                INNER JOIN users u ON (b.created_by_id = u.id)
                     GROUP BY u.user_name, cws.addr_src_digits
                     ORDER BY u.user_name
                )
                SELECT Account,
                       Sender,
                       Total_Messages,
                       CAST(SUM(Total_Messages) OVER() AS BIGINT) AS total_count
                FROM summary_cdr
            """;


    public static final String COUNT_QUERY = String.format("SELECT COUNT(*) FROM %s %s %%s", TABLE_CDR_NAME, TABLE_ALIAS_CDR);

    public static final String COUNT_QUERY_BY_REPORT_TABLE = "SELECT COUNT(*) FROM %s";

    public static final String DROP_REPORT_TABLE = "DROP TABLE IF EXISTS %s";
}
