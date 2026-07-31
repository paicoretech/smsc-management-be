CREATE KEYSPACE smsc
    WITH replication = {'class': 'NetworkTopologyStrategy', 'datacenter1': 1};

USE smsc;

CREATE TABLE IF NOT EXISTS smpp_submit_sm_result
(
    id   TEXT PRIMARY KEY,
    data TEXT
);

CREATE TABLE IF NOT EXISTS http_submit_sm_result
(
    id   TEXT PRIMARY KEY,
    data TEXT
);

CREATE TABLE IF NOT EXISTS smpp_message_parts
(
    id   TEXT PRIMARY KEY,
    data TEXT
);

CREATE TABLE IF NOT EXISTS smpp_pending_dlr
(
    network_id int,
    message_id text,
    timestamp  timeuuid,
    data       text,
    PRIMARY KEY (network_id, timestamp, message_id)
) WITH CLUSTERING ORDER BY (timestamp ASC, message_id ASC);

CREATE TABLE IF NOT EXISTS retries
(
    timestamp timeuuid,
    send_time bigint,
    data      text,
    PRIMARY KEY (send_time, timestamp)
) WITH CLUSTERING ORDER BY (timestamp ASC);

CREATE TABLE IF NOT EXISTS message_parts
(
    id   TEXT PRIMARY KEY,
    data TEXT
);

CREATE TABLE IF NOT EXISTS message_error_network
(
    key        text,
    message_id text,
    data       text,
    id         text,
    PRIMARY KEY (key, message_id)
);

CREATE TABLE IF NOT EXISTS dnd_entries
(
    parent_id int,
    key       text,
    dnd_type  text,
    msisdn    text,
    PRIMARY KEY (dnd_type, msisdn)
);