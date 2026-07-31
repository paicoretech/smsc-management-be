package com.smsc.management.app.broadcast.utils;

import com.paicbd.smsc.utils.Generated;

@Generated
public class HelperBroadcastQuery {
    private HelperBroadcastQuery() {
        throw new IllegalStateException("Utility class");
    }

    // query native for broadcast execution
    public static final String BROADCAST_MESSAGE_ID = "message_id";
    public static final String BROADCAST_STATUS = "status";
    public static final String BROADCAST_MESSAGE = "message";
    public static final String BROADCAST_COLUMN_MAPPING = "column_mapping_data_str";
    public static final String BROADCAST_SOURCE_ADDR = "source_addr";
    public static final String BROADCAST_DESTINATION_ADDR = "destination_addr";
    public static final String PROCESSING_BATCH_SIZE = "batchSize";
    public static final String BROADCAST_COMMENT = "comment";

    public static final String BROADCAST_FILTER_ENQUEUE_AT = "enqueueAt";
    public static final String BROADCAST_FILTER_SOURCE_ADDR = "sourceAddr";
    public static final String BROADCAST_FILTER_DESTINATION_ADDR = "destinationAddr";
    public static final String BROADCAST_FILTER_ID = "broadcastId";
    public static final String BROADCAST_FILTER_STATUS = "filterStatus";
    public static final String BROADCAST_FILTER_MESSAGE_ID = "messageId";

    public static final String PAGINATION_BROADCAST_DEVICES_INIT = """
            SELECT message_id, message, column_mapping_data_str
            FROM broadcast_devices
            WHERE broadcast_id = :broadcastId AND status = :filterStatus
            ORDER BY message_id
            LIMIT :batchSize;
            """;

    public static final String PAGINATION_BROADCAST_DEVICES = """
            SELECT message_id, message, column_mapping_data_str
            FROM broadcast_devices
            WHERE broadcast_id = :broadcastId AND status = :filterStatus
            AND message_id > :messageId
            ORDER BY message_id
            LIMIT :batchSize;
            """;

    public static final String UPDATE_BROADCAST_DEVICES_BATCH_POSITIONAL = """
            UPDATE broadcast_devices
            SET status = ?,
                enqueue_at = (?)::timestamp,
                source_addr = ?,
                destination_addr = ?,
                comment = ?
            WHERE broadcast_id = ? AND message_id = ?;
            """;

    public static final String UPDATE_BROADCAST_DEVICES_FOR_MAX_EXECUTION = """
            UPDATE broadcast_devices
            SET status = :status,
                comment = :comment,
                enqueue_at = :enqueueAt
            WHERE broadcast_id = :broadcastId AND status = :filterStatus;
            """;

    public static final String LOAD_BROADCAST_FILE_QUERY = """
            INSERT INTO broadcast_devices (
                broadcast_id,
                message_id,
                column_mapping_data_str,
                comment,
                dest_name,
                dest_network_id,
                dest_network_type,
                dest_protocol,
                destination_addr,
                enqueue_at,
                message,
                sent_at,
                source_addr,
                status
            ) VALUES (
                :broadcastId,
                :messageId,
                :columnMappingDataStr,
                :comment,
                :destName,
                :destNetworkId,
                :destNetworkType,
                :destProtocol,
                :destinationAddr,
                :enqueueAt,
                :message,
                :sentAt,
                :sourceAddr,
                :status
            )
            ON CONFLICT (broadcast_id, message, destination_addr)
            DO UPDATE SET count_duplicated = broadcast_devices.count_duplicated + 1;
            """;

    public static final String BROADCAST_STATISTICS_QUERY = """
            SELECT
                COUNT(*)                             AS total,
                COUNT(*) FILTER (WHERE status = 1)   AS pending,
                COUNT(*) FILTER (WHERE status = 2)   AS enqueue,
                COUNT(*) FILTER (WHERE status = 3)   AS sent,
                COUNT(*) FILTER (WHERE status = 4)   AS failed,
                COUNT(*) FILTER (WHERE status = 5)   AS duplicated,
                COALESCE(SUM(count_duplicated), 0)   AS duplicated_attempts,
                COUNT(*) FILTER (WHERE status = 6)   AS invalid
            FROM broadcast_devices
            WHERE broadcast_id = :broadcastId;
           """;

    public static final String UPDATE_BROADCAST_MESSAGE_TEMPLATE = """
            UPDATE broadcast_devices
            SET message = :message,
            comment = :comment
            WHERE broadcast_id = :broadcastId AND message_id = :messageId AND status = :filterStatus;
            """;
}
