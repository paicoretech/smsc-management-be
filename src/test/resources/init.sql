CREATE TABLE public.balance_type
(
    name character varying(255) NOT NULL
);



--
-- Name: bind_statuses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bind_statuses
(
    state character varying(255) NOT NULL
);



--
-- Name: binds_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.binds_types
(
    _type       character varying(255) NOT NULL,
    use_gateway boolean DEFAULT true   NOT NULL,
    use_sp      boolean DEFAULT true   NOT NULL
);



--
-- Name: broadcast; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.broadcast
(
    id                     integer NOT NULL,
    created_at             timestamp(6) without time zone,
    created_by_id          integer,
    updated_at             timestamp(6) without time zone,
    updated_by_id          integer,
    column_mapping         text,
    comment                text    DEFAULT ''::text NOT NULL,
    data_coding            integer,
    description            character varying(255),
    dest_addr_npi          integer,
    dest_addr_ton          integer,
    file_id                integer,
    first_record_mapping   text,
    is_immediate           boolean DEFAULT false,
    max_execution_datetime timestamp(6) without time zone,
    message_template       text,
    name                   character varying(255),
    network_id             integer NOT NULL,
    request_dlr            boolean DEFAULT false,
    sender_id              character varying(255),
    source_addr_npi        integer,
    source_addr_ton        integer,
    start_datetime         timestamp(6) without time zone,
    status                 character varying(255),
    CONSTRAINT broadcast_status_check CHECK (((status)::text = ANY ((ARRAY['CREATING':: character varying, 'CREATED':: character varying, 'FAILED':: character varying, 'PROCESSING':: character varying, 'COMPLETED':: character varying, 'SCHEDULED':: character varying, 'DRAFT':: character varying, 'UPDATING':: character varying, 'APPROVED':: character varying, 'REJECTED':: character varying, 'PENDING':: character varying, 'CANCELED':: character varying, 'DELETED':: character varying])::text[])
) )
);



--
-- Name: broadcast_devices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.broadcast_devices
(
    message_id              character varying(255) NOT NULL,
    broadcast_id            integer                NOT NULL,
    column_mapping_data_str text    DEFAULT '{}'::text NOT NULL,
    comment                 text    DEFAULT ''::text NOT NULL,
    dest_name               text    DEFAULT ''::text NOT NULL,
    dest_network_id         integer DEFAULT 0      NOT NULL,
    dest_network_type       text    DEFAULT ''::text NOT NULL,
    dest_protocol           text    DEFAULT ''::text NOT NULL,
    destination_addr        character varying(255),
    enqueue_at              timestamp(6) without time zone,
    message                 text    DEFAULT ''::text NOT NULL,
    sent_at                 timestamp(6) without time zone,
    source_addr             character varying(255),
    status                  integer DEFAULT 1      NOT NULL,
    count_duplicated         integer DEFAULT 0      NOT NULL
);



--
-- Name: broadcast_file; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.broadcast_file
(
    id         integer NOT NULL,
    columns    text,
    created_at timestamp(6) without time zone,
    delimiter  character varying(255),
    filename   character varying(255),
    has_header boolean DEFAULT false,
    size_bytes bigint  DEFAULT 0,
    status     character varying(255),
    token      character varying(255),
    total_rows integer,
    type       text    DEFAULT 'BROADCAST'::text,
    updated_at timestamp(6) without time zone,
    CONSTRAINT broadcast_file_status_check CHECK (((status)::text = ANY ((ARRAY['CREATING':: character varying, 'CREATED':: character varying, 'FAILED':: character varying, 'PROCESSING':: character varying, 'COMPLETED':: character varying, 'SCHEDULED':: character varying, 'DRAFT':: character varying, 'UPDATING':: character varying, 'APPROVED':: character varying, 'REJECTED':: character varying, 'PENDING':: character varying, 'CANCELED':: character varying, 'DELETED':: character varying])::text[])
) )
);



--
-- Name: broadcast_file_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.broadcast_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: broadcast_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.broadcast_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: callback_header_http; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.callback_header_http
(
    id             integer NOT NULL,
    header_name    character varying(255),
    header_value   character varying(255),
    interpreter_id integer,
    network_id     integer
);



--
-- Name: callback_header_http_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.callback_header_http_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: common_variables; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.common_variables
(
    key              character varying(255) NOT NULL,
    data_type        character varying(255),
    redis_replicated boolean,
    value            character varying(255)
);



--
-- Name: credit_sales_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.credit_sales_history
(
    id            integer NOT NULL,
    created_at    timestamp(6) without time zone,
    created_by_id integer,
    updated_at    timestamp(6) without time zone,
    updated_by_id integer,
    credit        bigint DEFAULT 0,
    description   character varying(255),
    network_id    integer
);



--
-- Name: credit_sales_history_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.credit_sales_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: custom_param_matcher; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.custom_param_matcher
(
    id              integer NOT NULL,
    property_name   character varying(255),
    routing_rule_id integer,
    value_matcher   character varying(255)
);



--
-- Name: custom_param_matcher_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.custom_param_matcher_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: delivery_error_code; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_error_code
(
    id            integer                NOT NULL,
    created_at    timestamp(6) without time zone,
    created_by_id integer,
    updated_at    timestamp(6) without time zone,
    updated_by_id integer,
    code          character varying(255),
    description   character varying(255) NOT NULL
);



--
-- Name: delivery_error_code_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_error_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: delivery_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_status
(
    value character varying(255) NOT NULL,
    name  character varying(255)
);



--
-- Name: diameter_application; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.diameter_application
(
    id                     integer NOT NULL,
    acct_appl_id           integer,
    auth_appl_id           integer,
    diameter_local_peer_id integer,
    diameter_realm_id      integer,
    name                   character varying(255),
    vendor_id              integer
);



--
-- Name: diameter_application_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.diameter_application_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: diameter_gateway; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.diameter_gateway
(
    id                     integer NOT NULL,
    connection_type        character varying(255),
    global_title           character varying(255),
    diameter_local_peer_id integer,
    mno_id                 integer,
    name                   character varying(255),
    network_id             integer,
    diameter_parameters_id integer,
    protocol               text    DEFAULT 'DIAMETER'::text,
    split_message          boolean DEFAULT false,
    started                boolean NOT NULL,
    type                   character varying(255),
    is_deleted             boolean default false,
    hss_update_enabled     boolean default false not null,
    allowed_traffic        boolean default true not null,
    allowed_ussi           boolean default false not null,
    created_at             timestamp(6),
    created_by_id          integer,
    updated_at             timestamp(6),
    updated_by_id          integer,
    messages_per_second_high      integer DEFAULT 0,
    messages_per_second_medium    integer DEFAULT 0,
    messages_per_second_low       integer DEFAULT 0,
    messages_per_second           integer DEFAULT 0,
    CONSTRAINT diameter_gateway_connection_type_check CHECK (((connection_type)::text = ANY ((ARRAY['TCP':: character varying, 'SCTP':: character varying])::text[])
) )
);



--
-- Name: diameter_gateway_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.diameter_gateway_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: diameter_local_peer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.diameter_local_peer
(
    id                  integer NOT NULL,
    diameter_gateway_id integer,
    firmware_version    integer,
    ip_addresses        character varying(255),
    product_name        character varying(255),
    realm               character varying(255),
    uri                 character varying(255),
    vendor_id           integer
);



--
-- Name: diameter_local_peer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.diameter_local_peer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: diameter_parameters; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.diameter_parameters
(
    id                       integer NOT NULL,
    accept_undefined_peer    boolean NOT NULL,
    bind_delay               bigint,
    cea_time_out             integer,
    diameter_gateway_id      integer,
    dpa_time_out             integer,
    duplicate_protection     boolean NOT NULL,
    duplicate_size           integer,
    duplicate_timer          integer,
    dwa_time_out             integer,
    iac_time_out             integer,
    message_time_out         integer,
    peer_fsm_thread_count    integer,
    queue_size               integer,
    rec_time_out             integer,
    request_table_clear_size integer,
    request_table_size       integer,
    session_time_out         bigint,
    single_local_peer        boolean NOT NULL,
    stop_time_out            integer,
    use_uri_as_fqdn          boolean NOT NULL
);



--
-- Name: diameter_parameters_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.diameter_parameters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: diameter_peer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.diameter_peer
(
    id                  integer NOT NULL,
    applications        character varying(255),
    attempt_connect     boolean NOT NULL,
    diameter_gateway_id integer,
    host                character varying(255),
    ip                  character varying(255),
    name                character varying(255),
    port_range          character varying(255),
    rating              integer NOT NULL,
    security_ref        character varying(255),
    standby_addresses   character varying(255),
    started             boolean NOT NULL,
    uri                 character varying(255)
);



--
-- Name: diameter_peer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.diameter_peer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: diameter_realm; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.diameter_realm
(
    id                      integer NOT NULL,
    diameter_application_id integer,
    diameter_gateway_id     integer,
    dynamic                 boolean NOT NULL,
    exp_time                integer NOT NULL,
    local_action            character varying(255),
    name                    character varying(255),
    peers                   character varying(255),
    uri                     character varying(255)
);



--
-- Name: diameter_realm_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.diameter_realm_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: dnd_entry_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dnd_entry_list
(
    id            integer                NOT NULL,
    created_at    timestamp(6) without time zone,
    created_by_id integer,
    updated_at    timestamp(6) without time zone,
    updated_by_id integer,
    dnd_type      character varying(255) NOT NULL,
    dnd_value     character varying(255) NOT NULL,
    name          character varying(255) NOT NULL,
    status        character varying(255) NOT NULL,
    comment       text,
    CONSTRAINT dnd_entry_list_dnd_type_check CHECK (((dnd_type)::text = ANY ((ARRAY['GLOBAL':: character varying, 'NETWORK_ID':: character varying, 'SENDER':: character varying])::text[])
) ),
    CONSTRAINT dnd_entry_list_status_check CHECK (((status)::text = ANY ((ARRAY['CREATING'::character varying, 'ACTIVATING'::character varying, 'ACTIVE'::character varying, 'DISABLED'::character varying, 'FAILED'::character varying])::text[])))
);



--
-- Name: dnd_entry_list_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dnd_entry_list_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: dnd_entry_list_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.dnd_entry_list_id_seq OWNED BY public.dnd_entry_list.id;


--
-- Name: dnd_entry_msidn; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dnd_entry_msidn
(
    id        bigint                 NOT NULL,
    msisdn    character varying(255) NOT NULL,
    parent_id integer                NOT NULL
);



--
-- Name: dnd_entry_msidn_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dnd_entry_msidn_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: dnd_entry_msidn_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.dnd_entry_msidn_id_seq OWNED BY public.dnd_entry_msidn.id;


--
-- Name: encoding_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.encoding_type
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: error_code; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.error_code
(
    id            integer                NOT NULL,
    created_at    timestamp(6) without time zone,
    created_by_id integer,
    updated_at    timestamp(6) without time zone,
    updated_by_id integer,
    code          character varying(255),
    description   character varying(255) NOT NULL,
    mno_id        integer
);



--
-- Name: error_code_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.error_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: error_code_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.error_code_mapping
(
    id                     integer NOT NULL,
    created_at             timestamp(6) without time zone,
    created_by_id          integer,
    updated_at             timestamp(6) without time zone,
    updated_by_id          integer,
    delivery_error_code_id integer,
    delivery_status_id     character varying(255),
    error_code_id          integer
);



--
-- Name: error_code_mapping_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.error_code_mapping_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: functionality; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.functionality
(
    id   character varying(255) NOT NULL,
    name character varying(255)
);



--
-- Name: gateways; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gateways
(
    network_id                             integer             NOT NULL,
    created_at                             timestamp(6) without time zone,
    created_by_id                          integer,
    updated_at                             timestamp(6) without time zone,
    updated_by_id                          integer,
    active_sessions_numbers                integer,
    address_npi                            integer DEFAULT 0   NOT NULL,
    address_range                          text    DEFAULT '^[0-9a-zA-Z]*'::text NOT NULL,
    address_ton                            integer DEFAULT 0   NOT NULL,
    authentication_types                   character varying(255),
    auto_retry_error_code                  text    DEFAULT ''::text,
    bind_retry_period                      integer DEFAULT 10000,
    bind_timeout                           integer DEFAULT 5000,
    bind_type                              text                NOT NULL,
    enabled                                integer DEFAULT 0,
    encoding_gsm7                          integer,
    encoding_iso88591                      integer,
    encoding_ucs2                          integer,
    enquire_link_period                    integer DEFAULT 30000,
    external_id                            character varying(255),
    header_security_name                   character varying(255),
    interface_version                      text    DEFAULT 'IF_34'::text NOT NULL,
    ip                                     text                NOT NULL,
    mno_id                                 integer             NOT NULL,
    name                                   text                NOT NULL,
    no_retry_error_code                    character varying(255),
    passwd                                 character varying(255),
    password                               text                NOT NULL,
    pdu_degree                             integer DEFAULT 1   NOT NULL,
    pdu_timeout                            integer DEFAULT 5000,
    port                                   integer             NOT NULL,
    protocol                               character varying(255),
    request_dlr                            integer DEFAULT 2,
    retry_alternate_destination_error_code character varying(255),
    sessions_number                        integer DEFAULT 1   NOT NULL,
    split_message                          boolean DEFAULT false,
    split_smpp_type                        text    DEFAULT 'TLV'::text,
    status                                 text    DEFAULT 'CLOSED'::text,
    system_id                              text                NOT NULL,
    system_type                            text                NOT NULL,
    thread_pool_size                       integer DEFAULT 100 NOT NULL,
    token                                  character varying(255),
    user_name                              character varying(255),
    messages_per_second_high               integer DEFAULT 0,
    messages_per_second_medium             integer DEFAULT 0,
    messages_per_second_low                integer DEFAULT 0,
    messages_per_second                    integer DEFAULT 0,
    tls_enabled                            boolean DEFAULT false NOT NULL
);



--
-- Name: general_settings_smpp_http; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.general_settings_smpp_http
(
    id                  integer NOT NULL,
    dest_addr_npi       integer,
    dest_addr_ton       integer,
    encoding_gsm7       integer,
    encoding_iso88591   integer,
    encoding_ucs2       integer,
    max_validity_period integer,
    source_addr_npi     integer,
    source_addr_ton     integer,
    validity_period     integer
);



--
-- Name: general_smsc_retry; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.general_smsc_retry
(
    id                     integer NOT NULL,
    first_retry_delay      integer,
    max_due_delay          integer,
    retry_delay_multiplier integer
);



--
-- Name: global_title_indicator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.global_title_indicator
(
    gt_indicator_id character varying(255) NOT NULL,
    gt_indicator    character varying(255)
);



--
-- Name: interfaz_versions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.interfaz_versions
(
    id      character varying(255) NOT NULL,
    version character varying(255)
);



--
-- Name: interpreter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.interpreter
(
    id               integer               NOT NULL,
    body_type        character varying(255),
    default_template boolean DEFAULT false NOT NULL,
    direction        character varying(255),
    event_type       character varying(255),
    gateway_id       integer,
    path             character varying(255),
    template         text    DEFAULT ''::text NOT NULL,
    use_proxy        boolean DEFAULT false NOT NULL
);



--
-- Name: interpreter_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.interpreter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: load_sharing_algorithm; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.load_sharing_algorithm
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: m3ua; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua
(
    id                                  integer NOT NULL,
    cc_delay_back_to_normal_threshold_1 numeric,
    cc_delay_back_to_normal_threshold_2 numeric,
    cc_delay_back_to_normal_threshold_3 numeric,
    cc_delay_threshold_1                numeric,
    cc_delay_threshold_2                numeric,
    cc_delay_threshold_3                numeric,
    connect_delay                       integer,
    external_id                         character varying(255),
    heart_beat_time                     integer,
    max_for_route                       integer,
    max_sequence_number                 integer,
    network_id                          integer,
    routing_key_management_enabled      boolean,
    routing_label_format                character varying(255),
    thread_count                        integer,
    use_lowest_bit_for_link             boolean
);



--
-- Name: m3ua_app_servers_routes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua_app_servers_routes
(
    id                    integer NOT NULL,
    application_server_id integer,
    route_id              integer
);



--
-- Name: m3ua_app_servers_routes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_app_servers_routes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: m3ua_application_server; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua_application_server
(
    id                        integer NOT NULL,
    exchange                  character varying(255),
    functionality             character varying(255),
    minimum_asp_for_loadshare integer,
    name                      character varying(255),
    network_appearance        integer,
    routing_context           integer,
    state                     character varying(255),
    traffic_mode_id           integer
);



--
-- Name: m3ua_application_server_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_application_server_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: m3ua_ass_app_servers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua_ass_app_servers
(
    id                    integer NOT NULL,
    application_server_id integer,
    asp_id                integer
);



--
-- Name: m3ua_ass_app_servers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_ass_app_servers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: m3ua_associations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua_associations
(
    id             integer NOT NULL,
    asp_name       character varying(255),
    enabled        integer DEFAULT 0,
    m3ua_heartbeat boolean,
    m3ua_socket_id integer,
    name           character varying(255),
    peer           character varying(255),
    peer_port      integer,
    state          character varying(255)
);



--
-- Name: m3ua_associations_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_associations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: m3ua_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: m3ua_routes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua_routes
(
    id                     integer NOT NULL,
    destination_point_code integer,
    m3ua_id                integer,
    origination_point_code integer,
    service_indicator      integer,
    traffic_mode_id        integer
);



--
-- Name: m3ua_routes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_routes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: m3ua_sockets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.m3ua_sockets
(
    id                         integer NOT NULL,
    enabled                    integer DEFAULT 0,
    extra_address              character varying(255),
    host_address               character varying(255),
    host_port                  integer,
    max_concurrent_connections integer,
    name                       character varying(255),
    socket_type                character varying(255),
    ss7_m3ua_id                integer,
    state                      character varying(255),
    transport_type             character varying(255)
);



--
-- Name: m3ua_sockets_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.m3ua_sockets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: map; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.map
(
    id                         integer NOT NULL,
    external_id                character varying(255),
    forward_sm_service_op_code integer,
    network_id                 integer,
    sri_service_op_code        integer
);



--
-- Name: map_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.map_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: nature_of_address; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.nature_of_address
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: networks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.networks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: npi_catalog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.npi_catalog
(
    id          integer NOT NULL,
    description character varying(255)
);



--
-- Name: numbering_plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.numbering_plan
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: operator_mno; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.operator_mno
(
    id                        integer NOT NULL,
    created_at                timestamp(6) without time zone,
    created_by_id             integer,
    updated_at                timestamp(6) without time zone,
    updated_by_id             integer,
    enabled                   boolean DEFAULT true,
    message_id_decimal_format boolean,
    name                      character varying(255),
    tlv_message_receipt_id    boolean
);



--
-- Name: operator_mno_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.operator_mno_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: origination_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.origination_type
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: report_file; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.report_file
(
    id            integer NOT NULL,
    created_at    timestamp(6) without time zone,
    created_by_id integer,
    extension     character varying(255),
    filename      character varying(255),
    path          character varying(255),
    status        character varying(255),
    token         character varying(255),
    type          text DEFAULT 'CDRS'::text,
    updated_at    timestamp(6) without time zone,
    CONSTRAINT report_file_extension_check CHECK (((extension)::text = ANY ((ARRAY['CSV':: character varying, 'XLSX':: character varying, 'PDF':: character varying])::text[])
) ),
    CONSTRAINT report_file_status_check CHECK (((status)::text = ANY ((ARRAY['CREATING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'TOKEN_EXPIRED'::character varying])::text[])))
);



--
-- Name: report_file_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.report_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: routing_rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.routing_rules
(
    id                              integer NOT NULL,
    add_dest_addr_prefix            character varying(255),
    add_source_addr_prefix          character varying(255),
    apply_for_refund                boolean,
    check_sri_response              boolean DEFAULT false,
    diameter_charging               boolean,
    drop_map_sri                    boolean DEFAULT false,
    drop_temp_failure               boolean,
    is_sri_response                 boolean DEFAULT false,
    network_id_temp_failure         integer,
    network_id_to_map_sri           integer,
    network_id_to_permanent_failure integer,
    new_dest_addr_npi               integer,
    new_dest_addr_ton               integer,
    new_destination_addr            character varying(255),
    new_gt_sccp_addr                character varying(255),
    new_short_message               character varying(255),
    new_source_addr                 character varying(255),
    new_source_addr_npi             integer,
    new_source_addr_ton             integer,
    origin_network_id               integer,
    regex_calling_party_address     character varying(255),
    regex_dest_addr_npi             character varying(255),
    regex_dest_addr_ton             character varying(255),
    regex_destination_addr          character varying(255),
    regex_imsi_digits_mask          character varying(255),
    regex_network_node_number       character varying(255),
    regex_short_message             character varying(255),
    regex_source_addr               character varying(255),
    regex_source_addr_npi           character varying(255),
    regex_source_addr_ton           character varying(255),
    remove_dest_addr_prefix         character varying(255),
    remove_source_addr_prefix       character varying(255)
);



--
-- Name: routing_rules_action_advanced; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.routing_rules_action_advanced
(
    routing_rules_id                    integer                NOT NULL,
    application_context_mt              character varying(255) NOT NULL,
    custom_map_layer_service_centre_address_oa character varying(255) NOT NULL,
    custom_map_layer_source_address_mt  character varying(255) NOT NULL,
    custom_map_layer_source_address_sri character varying(255) NOT NULL,
    map_version                         integer                NOT NULL,
    operation_code_mt                   integer                NOT NULL,
    operation_code_sri                  integer                NOT NULL,
    priority_flag_sri                   boolean                NOT NULL,
    sccp_source_address_mt              character varying(255) NOT NULL,
    sccp_source_address_sri             character varying(255) NOT NULL,
    sccp_destination_address_mt         varchar(255) not null,
    sccp_destination_address_sri         varchar(255) not null,
    ssn_hlr_sri                         integer                NOT NULL,
    ssn_msc_mt                          integer                NOT NULL,
    ssn_smsc_mt                         integer                NOT NULL,
    ssn_smsc_sri                        integer                NOT NULL
);



--
-- Name: routing_rules_destination; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.routing_rules_destination
(
    id               integer NOT NULL,
    network_id       integer,
    network_type     character varying(255),
    priority         integer NOT NULL,
    routing_rules_id integer
);



--
-- Name: routing_rules_destination_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.routing_rules_destination_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: routing_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.routing_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: rule_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rule_type
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: sccp; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp
(
    id                                 integer NOT NULL,
    congestion_control                 boolean,
    congestion_control_algorithm       character varying(255),
    congestion_control_timer_a         integer,
    congestion_control_timer_d         integer,
    external_id                        character varying(255),
    max_data_message                   integer,
    network_id                         integer,
    period_of_logging                  integer,
    preview_mode                       boolean,
    reassembly_timer_delay             integer,
    remove_spc                         boolean,
    sccp_protocol_version              character varying(255),
    sst_timer_duration_increase_factor numeric,
    sst_timer_duration_max             integer,
    sst_timer_duration_min             integer,
    z_margin_xudt_message              integer,
    rsp_prohibited_by_default          boolean
);



--
-- Name: sccp_addresses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp_addresses
(
    id                   integer NOT NULL,
    address_indicator    integer,
    digits               character varying(255),
    gt_indicator         character varying(255),
    name                 character varying(255),
    nature_of_address_id integer,
    numbering_plan_id    integer,
    point_code           integer,
    ss7_sccp_id          integer,
    subsystem_number     integer,
    translation_type     integer
);



--
-- Name: sccp_addresses_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sccp_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sccp_mtp3_destinations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp_mtp3_destinations
(
    id               integer NOT NULL,
    first_point_code integer,
    first_sls        integer,
    last_point_code  integer,
    last_sls         integer,
    name             character varying(255),
    sccp_sap_id      integer,
    sls_mask         integer
);



--
-- Name: sccp_mtp3_destinations_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_mtp3_destinations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sccp_remote_resources; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp_remote_resources
(
    id                 integer NOT NULL,
    mark_prohibited    boolean,
    remote_sccp_status character varying(255),
    remote_spc         integer,
    remote_spc_status  character varying(255),
    remote_ssn         integer,
    remote_ssn_status  character varying(255),
    ss7_sccp_id        integer
);



--
-- Name: sccp_remote_resources_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_remote_resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sccp_rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp_rules
(
    id                           integer NOT NULL,
    address_indicator            integer,
    calling_address_indicator    integer,
    calling_global_tittle_digits character varying(255),
    calling_gt_indicator         character varying(255),
    calling_nature_of_address_id integer,
    calling_numbering_plan_id    integer,
    calling_point_code           integer,
    calling_subsystem_number     integer,
    calling_translator_type      integer,
    global_tittle_digits         character varying(255),
    gt_indicator                 character varying(255),
    load_sharing_algorithm_id    integer,
    mask                         character varying(255),
    name                         character varying(255),
    nature_of_address_id         integer,
    new_calling_party_address    character varying(255),
    numbering_plan_id            integer,
    origination_type_id          integer,
    point_code                   integer,
    primary_address_id           integer,
    rule_type_id                 integer,
    secondary_address_id         integer,
    subsystem_number             integer,
    translation_type             integer
);



--
-- Name: sccp_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sccp_service_access_points; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp_service_access_points
(
    id                integer NOT NULL,
    local_gt_digits   character varying(255),
    name              character varying(255),
    network_indicator integer,
    origin_point_code integer,
    ss7_sccp_id       integer
);



--
-- Name: sccp_service_access_points_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_service_access_points_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sccp_long_message_rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sccp_long_message_rules
(
    id                      integer NOT NULL,
    first_point_code        integer,
    last_point_code         integer,
    long_message_rule_type  character varying(255),
    sccp_sap_id             integer
);



--
-- Name: sccp_long_message_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sccp_long_message_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: sequence_networks_id; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sequence_networks_id
(
    id           integer NOT NULL,
    network_type character varying(255)
);



--
-- Name: service_provider; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.service_provider
(
    network_id              integer           NOT NULL,
    created_at              timestamp(6) without time zone,
    created_by_id           integer,
    updated_at              timestamp(6) without time zone,
    updated_by_id           integer,
    active_sessions_numbers integer,
    address_npi             integer DEFAULT 0 NOT NULL,
    address_range           text    DEFAULT '^[0-9a-zA-Z]*'::text NOT NULL,
    address_ton             integer DEFAULT 0 NOT NULL,
    authentication_types    character varying(255),
    balance                 bigint  DEFAULT 0,
    balance_type            text    DEFAULT 'PREPAID'::text,
    bind_type               text    DEFAULT 'TRANSCEIVER'::text NOT NULL,
    callback_url            character varying(255),
    contact_name            character varying(255),
    email                   character varying(255),
    enabled                 integer DEFAULT 0,
    enquire_link_period     integer DEFAULT 30000,
    external_id             character varying(255),
    header_security_name    character varying(255),
    interface_version       text    DEFAULT 'IF_34'::text NOT NULL,
    name                    text              NOT NULL,
    passwd                  character varying(255),
    password                text              NOT NULL,
    pdu_timeout             integer DEFAULT 5000,
    phone_number            character varying(255),
    protocol                character varying(255),
    sessions_number         integer DEFAULT 1 NOT NULL,
    smpp_server_id          integer,
    status                  text    DEFAULT 'CLOSED'::text,
    system_id               text              NOT NULL,
    system_type             text              NOT NULL,
    token                   character varying(255),
    tps                     integer DEFAULT 1,
    user_name               character varying(255),
    validity                integer DEFAULT 0,
    message_priority        text    DEFAULT 'Medium'::text NOT NULL,
    custom_parameters       text,
    bearer_token_expiration_seconds bigint,
    bearer_security_token_jti character varying(64),
    api_key_security_token text,
    basic_security_password text,
    security_authentication_type character varying(255),
    bearer_security_token_expires_at bigint,
    proxy_mode              boolean DEFAULT false NOT NULL,
    dlr_tlv_enabled         boolean DEFAULT false NOT NULL
);



--
-- Name: sls_range; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sls_range
(
    id   character varying(255) NOT NULL,
    name character varying(255)
);



--
-- Name: smpp_server; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.smpp_server
(
    id                integer               NOT NULL,
    created_at        timestamp(6) without time zone,
    created_by_id     integer,
    updated_at        timestamp(6) without time zone,
    updated_by_id     integer,
    action_status     text    DEFAULT ''::text NOT NULL,
    enabled           integer               NOT NULL,
    ip                character varying(255),
    is_default        boolean DEFAULT false NOT NULL,
    name              character varying(255),
    port              integer,
    processor_degree  integer,
    queue_capacity    integer,
    status            character varying(255),
    transaction_timer integer,
    tls_enabled       boolean DEFAULT false NOT NULL,
    wait_for_bind     integer
);



--
-- Name: smpp_server_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.smpp_server_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: ss7_gateways; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ss7_gateways
(
    network_id             integer NOT NULL,
    created_at             timestamp(6) without time zone,
    created_by_id          integer,
    updated_at             timestamp(6) without time zone,
    updated_by_id          integer,
    enabled                integer DEFAULT 1,
    external_id            character varying(255),
    global_title           text    DEFAULT ''::text,
    global_title_indicator text    DEFAULT '0100'::text,
    hlr_ssn                integer DEFAULT 6,
    map_version            integer DEFAULT 3,
    mno_id                 integer NOT NULL,
    msc_ssn                integer DEFAULT 8,
    name                   text    NOT NULL,
    protocol               text    DEFAULT 'SS7'::text,
    smsc_ssn               integer DEFAULT 8,
    split_message          boolean DEFAULT false,
    status                 text    DEFAULT 'STARTED'::text,
    translation_type       integer DEFAULT 0,
    home_routing           boolean default false,
    api_enabled            boolean default false not null,
    app_token              text,
    messages_per_second_high      integer DEFAULT 0,
    messages_per_second_medium    integer DEFAULT 0,
    messages_per_second_low       integer DEFAULT 0,
    messages_per_second           integer DEFAULT 0,
    hss_update_enabled     boolean default false not null,
    allowed_traffic        boolean default true not null,
    allowed_ussi           boolean default false not null
);



--
-- Name: tcap; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tcap
(
    id                              integer NOT NULL,
    blocking_incoming_tcap_messages boolean,
    dialog_id_range_end             integer,
    dialog_id_range_start           integer,
    dialog_idle_timeout             integer,
    do_not_send_protocol_version    boolean,
    exr_back_to_normal_delay_thr1   numeric,
    exr_back_to_normal_delay_thr2   numeric,
    exr_back_to_normal_delay_thr3   numeric,
    exr_delay_thr1                  numeric,
    exr_delay_thr2                  numeric,
    exr_delay_thr3                  numeric,
    external_id                     character varying(255),
    invoke_timeout                  integer,
    max_dialogs                     integer,
    mem_back_to_normal_delay_thr1   numeric,
    mem_back_to_normal_delay_thr2   numeric,
    mem_back_to_normal_delay_thr3   numeric,
    memory_monitor_thr1             numeric,
    memory_monitor_thr2             numeric,
    memory_monitor_thr3             numeric,
    network_id                      integer,
    preview_mode                    boolean,
    sls_range_id                    character varying(255),
    ssn_list                        character varying(255),
    swap_tcap_id_enabled            boolean
);



--
-- Name: tcap_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tcap_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: ton_catalog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ton_catalog
(
    id          integer NOT NULL,
    description character varying(255)
);



--
-- Name: traffic_mode; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.traffic_mode
(
    id   integer NOT NULL,
    name character varying(255)
);



--
-- Name: user_code_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;



--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles
(
    user_id integer NOT NULL,
    role    character varying(255)
);



--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users
(
    id                     integer                NOT NULL,
    created_at             timestamp(6) without time zone,
    created_by_id          integer,
    updated_at             timestamp(6) without time zone,
    updated_by_id          integer,
    account_locked         boolean DEFAULT false  NOT NULL,
    failed_login_attempts  integer DEFAULT 0      NOT NULL,
    jwt                    character varying(255),
    last_failed_login_time timestamp(6) without time zone,
    last_name              character varying(255),
    lock_time              timestamp(6) without time zone,
    login                  boolean DEFAULT false  NOT NULL,
    must_change_password   boolean DEFAULT true   NOT NULL,
    name                   character varying(255),
    password               character varying(255) NOT NULL,
    status                 smallint,
    user_name              character varying(255) NOT NULL,
    sender_id              character varying(500),
    all_service_providers  boolean DEFAULT false  NOT NULL
);

create table home_routing
(
    id          integer                             not null
        primary key,
    external_id varchar(255)
        constraint uk_qpwc2162spqjkg8v016ncnkoh
            unique,
    mode        text    default 'TRANSPARENT'::text not null
        constraint home_routing_mode_check
            check (mode = ANY (ARRAY ['TRANSPARENT'::text, 'NON_TRANSPARENT'::text])),
    network_id  integer                             not null,
    ttl_cache   integer default 300                 not null
);

-- auto-generated definition
create sequence home_routing_id_seq;

CREATE TABLE public.home_routing_cc_mcc_mnc
(
    id                  bigint PRIMARY KEY,
    country_code        text DEFAULT '-1' NOT NULL,
    mcc_mnc             text NOT NULL,
    smsc                text DEFAULT '',
    ss7_home_routing_id integer NOT NULL
        CONSTRAINT fkfe9qgmnm0pbq4e2gfjbs0jcnv
            REFERENCES home_routing(id)
);
-- auto-generated definition
create sequence home_routing_cc_mcc_mnc_id_seq;



--
-- Name: dnd_entry_list id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dnd_entry_list ALTER COLUMN id SET DEFAULT nextval('public.dnd_entry_list_id_seq'::regclass);


--
-- Name: dnd_entry_msidn id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dnd_entry_msidn ALTER COLUMN id SET DEFAULT nextval('public.dnd_entry_msidn_id_seq'::regclass);


--
-- Data for Name: balance_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.balance_type (name)
VALUES ('PREPAID');
INSERT INTO public.balance_type (name)
VALUES ('POSTPAID');


--
-- Data for Name: bind_statuses; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.bind_statuses (state)
VALUES ('STOPPED');
INSERT INTO public.bind_statuses (state)
VALUES ('STARTED');
INSERT INTO public.bind_statuses (state)
VALUES ('BINDING');
INSERT INTO public.bind_statuses (state)
VALUES ('BOUND');
INSERT INTO public.bind_statuses (state)
VALUES ('UNBINDING');
INSERT INTO public.bind_statuses (state)
VALUES ('UNBOUND');
INSERT INTO public.bind_statuses (state)
VALUES ('FORCE_STOPPED');



--
-- Data for Name: binds_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.binds_types (_type, use_gateway, use_sp)
VALUES ('TRANSMITTER', true, true);
INSERT INTO public.binds_types (_type, use_gateway, use_sp)
VALUES ('RECEIVER', true, true);
INSERT INTO public.binds_types (_type, use_gateway, use_sp)
VALUES ('TRANSCEIVER', true, true);
INSERT INTO public.binds_types (_type, use_gateway, use_sp)
VALUES ('ALL_BIND_TYPES', false, true);


--
-- Data for Name: broadcast; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: broadcast_devices; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: broadcast_file; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: callback_header_http; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: common_variables; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.common_variables (key, data_type, redis_replicated, value)
VALUES ('USE_LOCAL_CHARGING', 'boolean', false, 'true');
INSERT INTO public.common_variables (key, data_type, redis_replicated, value)
VALUES ('SMSC_ACCOUNT_SETTINGS', 'json', false, '{"max_password_length": 9, "max_system_id_length": 15}');
INSERT INTO public.common_variables (key, data_type, redis_replicated, value)
VALUES ('USE_ANALYZE', 'boolean', false, 'true');
INSERT INTO public.common_variables (key, data_type, redis_replicated, value)
VALUES ('USE_DND_FILTERING', 'boolean', true, 'true');


--
-- Data for Name: credit_sales_history; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: custom_param_matcher; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: delivery_error_code; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: delivery_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.delivery_status (value, name)
VALUES ('ENROUTE', 'ENROUTE');
INSERT INTO public.delivery_status (value, name)
VALUES ('DELIVRD', 'DELIVERED');
INSERT INTO public.delivery_status (value, name)
VALUES ('EXPIRED', 'EXPIRED');
INSERT INTO public.delivery_status (value, name)
VALUES ('DELETED', 'DELETED');
INSERT INTO public.delivery_status (value, name)
VALUES ('UNDELIV', 'UNDELIVERED');
INSERT INTO public.delivery_status (value, name)
VALUES ('ACCEPTD', 'ACCEPTED');
INSERT INTO public.delivery_status (value, name)
VALUES ('UNKNOWN', 'UNKNOWN');
INSERT INTO public.delivery_status (value, name)
VALUES ('REJECTD', 'REJECTED');


--
-- Data for Name: diameter_application; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: diameter_gateway; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: diameter_local_peer; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: diameter_parameters; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: diameter_peer; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: diameter_realm; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: dnd_entry_list; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: dnd_entry_msidn; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: encoding_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.encoding_type (id, name)
VALUES (0, 'GSM7');
INSERT INTO public.encoding_type (id, name)
VALUES (1, 'UTF8');
INSERT INTO public.encoding_type (id, name)
VALUES (2, 'UNICODE');
INSERT INTO public.encoding_type (id, name)
VALUES (3, 'ISO88591');


--
-- Data for Name: error_code; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: error_code_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: functionality; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.functionality (id, name)
VALUES ('SGW', 'SGW');
INSERT INTO public.functionality (id, name)
VALUES ('AS', 'AS');
INSERT INTO public.functionality (id, name)
VALUES ('IPSP-CLIENT', 'IPSP Client');
INSERT INTO public.functionality (id, name)
VALUES ('IPSP-SERVER', 'IPSP Server');


--
-- Data for Name: gateways; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: general_settings_smpp_http; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.general_settings_smpp_http (id, dest_addr_npi, dest_addr_ton, encoding_gsm7, encoding_iso88591,
                                               encoding_ucs2, max_validity_period, source_addr_npi, source_addr_ton,
                                               validity_period)
VALUES (1, 1, 1, 0, 3, 2, 240, 1, 1, 60);


--
-- Data for Name: general_smsc_retry; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.general_smsc_retry (id, first_retry_delay, max_due_delay, retry_delay_multiplier)
VALUES (1, 10, 86400, 2);


--
-- Data for Name: global_title_indicator; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.global_title_indicator (gt_indicator_id, gt_indicator)
VALUES ('GT0001', 'GLOBAL_TITLE_INCLUDES_NATURE_OF_ADDRESS_INDICATOR_ONLY');
INSERT INTO public.global_title_indicator (gt_indicator_id, gt_indicator)
VALUES ('GT0010', 'GLOBAL_TITLE_INCLUDES_TRANSLATION_TYPE_ONLY');
INSERT INTO public.global_title_indicator (gt_indicator_id, gt_indicator)
VALUES ('GT0011', 'GLOBAL_TITLE_INCLUDES_TRANSLATION_TYPE_NUMBERING_PLAN_AND_ENCODING_SCHEME');
INSERT INTO public.global_title_indicator (gt_indicator_id, gt_indicator)
VALUES ('GT0100', 'GLOBAL_TITLE_INCLUDES_TRANSLATION_TYPE_NUMBERING_PLAN_ENCODING_SCHEME_AND_NATURE_OF_ADDRESS');


--
-- Data for Name: interfaz_versions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.interfaz_versions (id, version)
VALUES ('IF_33', '3.3');
INSERT INTO public.interfaz_versions (id, version)
VALUES ('IF_34', '3.4');
INSERT INTO public.interfaz_versions (id, version)
VALUES ('IF_50', '5.0');
INSERT INTO public.interfaz_versions (id, version)
VALUES ('IF_ANY', 'ANY');


--
-- Data for Name: interpreter; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: load_sharing_algorithm; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.load_sharing_algorithm (id, name)
VALUES (1, 'UNDEFINED');
INSERT INTO public.load_sharing_algorithm (id, name)
VALUES (2, 'Bit0');
INSERT INTO public.load_sharing_algorithm (id, name)
VALUES (3, 'Bit1');
INSERT INTO public.load_sharing_algorithm (id, name)
VALUES (4, 'Bit2');
INSERT INTO public.load_sharing_algorithm (id, name)
VALUES (5, 'Bit3');
INSERT INTO public.load_sharing_algorithm (id, name)
VALUES (6, 'Bit4');


--
-- Data for Name: m3ua; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: m3ua_app_servers_routes; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: m3ua_application_server; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: m3ua_ass_app_servers; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: m3ua_associations; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: m3ua_routes; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: m3ua_sockets; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: map; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: nature_of_address; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.nature_of_address (id, name)
VALUES (-1, 'UNDEFINED');
INSERT INTO public.nature_of_address (id, name)
VALUES (0, ' UNKNOWN');
INSERT INTO public.nature_of_address (id, name)
VALUES (1, ' SUBSCRIBER');
INSERT INTO public.nature_of_address (id, name)
VALUES (2, ' RESERVED_NATIONAL_2');
INSERT INTO public.nature_of_address (id, name)
VALUES (3, ' NATIONAL');
INSERT INTO public.nature_of_address (id, name)
VALUES (4, ' INTERNATIONAL');
INSERT INTO public.nature_of_address (id, name)
VALUES (5, ' SPARE_5');
INSERT INTO public.nature_of_address (id, name)
VALUES (6, ' SPARE_6');
INSERT INTO public.nature_of_address (id, name)
VALUES (7, ' SPARE_7');
INSERT INTO public.nature_of_address (id, name)
VALUES (8, ' SPARE_8');
INSERT INTO public.nature_of_address (id, name)
VALUES (9, ' SPARE_9');
INSERT INTO public.nature_of_address (id, name)
VALUES (10, 'SPARE_10');
INSERT INTO public.nature_of_address (id, name)
VALUES (11, ' SPARE_11');
INSERT INTO public.nature_of_address (id, name)
VALUES (12, ' SPARE_12');
INSERT INTO public.nature_of_address (id, name)
VALUES (13, ' SPARE_13');
INSERT INTO public.nature_of_address (id, name)
VALUES (14, ' SPARE_14');
INSERT INTO public.nature_of_address (id, name)
VALUES (15, ' SPARE_15');
INSERT INTO public.nature_of_address (id, name)
VALUES (16, ' SPARE_16');
INSERT INTO public.nature_of_address (id, name)
VALUES (17, ' SPARE_17');
INSERT INTO public.nature_of_address (id, name)
VALUES (18, ' SPARE_18');
INSERT INTO public.nature_of_address (id, name)
VALUES (19, ' SPARE_19');
INSERT INTO public.nature_of_address (id, name)
VALUES (20, ' SPARE_20');
INSERT INTO public.nature_of_address (id, name)
VALUES (21, ' SPARE_21');
INSERT INTO public.nature_of_address (id, name)
VALUES (22, ' SPARE_22');
INSERT INTO public.nature_of_address (id, name)
VALUES (23, 'SPARE_23');
INSERT INTO public.nature_of_address (id, name)
VALUES (24, ' SPARE_24');
INSERT INTO public.nature_of_address (id, name)
VALUES (25, ' SPARE_25');
INSERT INTO public.nature_of_address (id, name)
VALUES (26, ' SPARE_26');
INSERT INTO public.nature_of_address (id, name)
VALUES (27, ' SPARE_27');
INSERT INTO public.nature_of_address (id, name)
VALUES (28, ' SPARE_28');
INSERT INTO public.nature_of_address (id, name)
VALUES (29, ' SPARE_29');
INSERT INTO public.nature_of_address (id, name)
VALUES (30, ' SPARE_30');
INSERT INTO public.nature_of_address (id, name)
VALUES (31, ' SPARE_31');
INSERT INTO public.nature_of_address (id, name)
VALUES (32, ' SPARE_32');
INSERT INTO public.nature_of_address (id, name)
VALUES (33, ' SPARE_33');
INSERT INTO public.nature_of_address (id, name)
VALUES (34, ' SPARE_34');
INSERT INTO public.nature_of_address (id, name)
VALUES (35, ' SPARE_35');
INSERT INTO public.nature_of_address (id, name)
VALUES (36, 'SPARE_36');
INSERT INTO public.nature_of_address (id, name)
VALUES (37, ' SPARE_37');
INSERT INTO public.nature_of_address (id, name)
VALUES (38, ' SPARE_38');
INSERT INTO public.nature_of_address (id, name)
VALUES (39, ' SPARE_39');
INSERT INTO public.nature_of_address (id, name)
VALUES (40, ' SPARE_40');
INSERT INTO public.nature_of_address (id, name)
VALUES (41, ' SPARE_41');
INSERT INTO public.nature_of_address (id, name)
VALUES (42, ' SPARE_42');
INSERT INTO public.nature_of_address (id, name)
VALUES (43, ' SPARE_43');
INSERT INTO public.nature_of_address (id, name)
VALUES (44, ' SPARE_44');
INSERT INTO public.nature_of_address (id, name)
VALUES (45, ' SPARE_45');
INSERT INTO public.nature_of_address (id, name)
VALUES (46, ' SPARE_46');
INSERT INTO public.nature_of_address (id, name)
VALUES (47, ' SPARE_47');
INSERT INTO public.nature_of_address (id, name)
VALUES (48, ' SPARE_48');
INSERT INTO public.nature_of_address (id, name)
VALUES (49, ' SPARE_49');
INSERT INTO public.nature_of_address (id, name)
VALUES (50, ' SPARE_50');
INSERT INTO public.nature_of_address (id, name)
VALUES (51, ' SPARE_51');
INSERT INTO public.nature_of_address (id, name)
VALUES (52, ' SPARE_52');
INSERT INTO public.nature_of_address (id, name)
VALUES (53, ' SPARE_53');
INSERT INTO public.nature_of_address (id, name)
VALUES (54, ' SPARE_54');
INSERT INTO public.nature_of_address (id, name)
VALUES (55, ' SPARE_55');
INSERT INTO public.nature_of_address (id, name)
VALUES (56, ' SPARE_56');
INSERT INTO public.nature_of_address (id, name)
VALUES (57, ' SPARE_57');
INSERT INTO public.nature_of_address (id, name)
VALUES (58, ' SPARE_58');
INSERT INTO public.nature_of_address (id, name)
VALUES (59, ' SPARE_59');
INSERT INTO public.nature_of_address (id, name)
VALUES (60, ' SPARE_60');
INSERT INTO public.nature_of_address (id, name)
VALUES (61, ' SPARE_61');
INSERT INTO public.nature_of_address (id, name)
VALUES (62, ' SPARE_62');
INSERT INTO public.nature_of_address (id, name)
VALUES (63, ' SPARE_63');
INSERT INTO public.nature_of_address (id, name)
VALUES (64, ' SPARE_64');
INSERT INTO public.nature_of_address (id, name)
VALUES (65, ' SPARE_65');
INSERT INTO public.nature_of_address (id, name)
VALUES (66, ' SPARE_66');
INSERT INTO public.nature_of_address (id, name)
VALUES (67, ' SPARE_67');
INSERT INTO public.nature_of_address (id, name)
VALUES (68, ' SPARE_68');
INSERT INTO public.nature_of_address (id, name)
VALUES (69, ' SPARE_69');
INSERT INTO public.nature_of_address (id, name)
VALUES (70, ' SPARE_70');
INSERT INTO public.nature_of_address (id, name)
VALUES (71, ' SPARE_71');
INSERT INTO public.nature_of_address (id, name)
VALUES (72, ' SPARE_72');
INSERT INTO public.nature_of_address (id, name)
VALUES (73, ' SPARE_73');
INSERT INTO public.nature_of_address (id, name)
VALUES (74, ' SPARE_74');
INSERT INTO public.nature_of_address (id, name)
VALUES (75, ' SPARE_75');
INSERT INTO public.nature_of_address (id, name)
VALUES (76, ' SPARE_76');
INSERT INTO public.nature_of_address (id, name)
VALUES (77, ' SPARE_77');
INSERT INTO public.nature_of_address (id, name)
VALUES (78, ' SPARE_78');
INSERT INTO public.nature_of_address (id, name)
VALUES (79, ' SPARE_79');
INSERT INTO public.nature_of_address (id, name)
VALUES (80, ' SPARE_80');
INSERT INTO public.nature_of_address (id, name)
VALUES (81, ' SPARE_81');
INSERT INTO public.nature_of_address (id, name)
VALUES (82, ' SPARE_82');
INSERT INTO public.nature_of_address (id, name)
VALUES (83, ' SPARE_83');
INSERT INTO public.nature_of_address (id, name)
VALUES (84, ' SPARE_84');
INSERT INTO public.nature_of_address (id, name)
VALUES (85, ' SPARE_85');
INSERT INTO public.nature_of_address (id, name)
VALUES (86, ' SPARE_86');
INSERT INTO public.nature_of_address (id, name)
VALUES (87, ' SPARE_87');
INSERT INTO public.nature_of_address (id, name)
VALUES (88, ' SPARE_88');
INSERT INTO public.nature_of_address (id, name)
VALUES (89, ' SPARE_89');
INSERT INTO public.nature_of_address (id, name)
VALUES (90, ' SPARE_90');
INSERT INTO public.nature_of_address (id, name)
VALUES (91, ' SPARE_91');
INSERT INTO public.nature_of_address (id, name)
VALUES (92, ' SPARE_92');
INSERT INTO public.nature_of_address (id, name)
VALUES (93, ' SPARE_93');
INSERT INTO public.nature_of_address (id, name)
VALUES (94, ' SPARE_94');
INSERT INTO public.nature_of_address (id, name)
VALUES (95, ' SPARE_95');
INSERT INTO public.nature_of_address (id, name)
VALUES (96, ' SPARE_96');
INSERT INTO public.nature_of_address (id, name)
VALUES (97, ' SPARE_97');
INSERT INTO public.nature_of_address (id, name)
VALUES (98, ' SPARE_98');
INSERT INTO public.nature_of_address (id, name)
VALUES (99, ' SPARE_99');
INSERT INTO public.nature_of_address (id, name)
VALUES (100, ' SPARE_100');
INSERT INTO public.nature_of_address (id, name)
VALUES (101, ' SPARE_101');
INSERT INTO public.nature_of_address (id, name)
VALUES (102, ' SPARE_102');
INSERT INTO public.nature_of_address (id, name)
VALUES (103, ' SPARE_103');
INSERT INTO public.nature_of_address (id, name)
VALUES (104, ' SPARE_104');
INSERT INTO public.nature_of_address (id, name)
VALUES (105, ' SPARE_105');
INSERT INTO public.nature_of_address (id, name)
VALUES (106, ' SPARE_106');
INSERT INTO public.nature_of_address (id, name)
VALUES (107, ' SPARE_107');
INSERT INTO public.nature_of_address (id, name)
VALUES (108, ' SPARE_108');
INSERT INTO public.nature_of_address (id, name)
VALUES (109, ' SPARE_109');
INSERT INTO public.nature_of_address (id, name)
VALUES (110, ' SPARE_110');
INSERT INTO public.nature_of_address (id, name)
VALUES (111, ' SPARE_111');
INSERT INTO public.nature_of_address (id, name)
VALUES (112, ' RESERVED_NATIONAL_112');
INSERT INTO public.nature_of_address (id, name)
VALUES (113, ' RESERVED_NATIONAL_113');
INSERT INTO public.nature_of_address (id, name)
VALUES (114, ' RESERVED_NATIONAL_114');
INSERT INTO public.nature_of_address (id, name)
VALUES (115, ' RESERVED_NATIONAL_115');
INSERT INTO public.nature_of_address (id, name)
VALUES (116, ' RESERVED_NATIONAL_116');
INSERT INTO public.nature_of_address (id, name)
VALUES (117, ' RESERVED_NATIONAL_117');
INSERT INTO public.nature_of_address (id, name)
VALUES (118, ' RESERVED_NATIONAL_118');
INSERT INTO public.nature_of_address (id, name)
VALUES (119, ' RESERVED_NATIONAL_119');
INSERT INTO public.nature_of_address (id, name)
VALUES (120, ' RESERVED_NATIONAL_120');
INSERT INTO public.nature_of_address (id, name)
VALUES (121, ' RESERVED_NATIONAL_121');
INSERT INTO public.nature_of_address (id, name)
VALUES (122, ' RESERVED_NATIONAL_122');
INSERT INTO public.nature_of_address (id, name)
VALUES (123, ' RESERVED_NATIONAL_123');
INSERT INTO public.nature_of_address (id, name)
VALUES (124, ' RESERVED_NATIONAL_124');
INSERT INTO public.nature_of_address (id, name)
VALUES (125, ' RESERVED_NATIONAL_125');
INSERT INTO public.nature_of_address (id, name)
VALUES (126, ' RESERVED_NATIONAL_126');
INSERT INTO public.nature_of_address (id, name)
VALUES (127, ' RESERVED');


--
-- Data for Name: npi_catalog; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.npi_catalog (id, description)
VALUES (-1, 'Default');
INSERT INTO public.npi_catalog (id, description)
VALUES (0, 'Unknown');
INSERT INTO public.npi_catalog (id, description)
VALUES (1, 'ISDN');
INSERT INTO public.npi_catalog (id, description)
VALUES (3, 'Data');
INSERT INTO public.npi_catalog (id, description)
VALUES (4, 'Telex');
INSERT INTO public.npi_catalog (id, description)
VALUES (6, 'Land Mobile');
INSERT INTO public.npi_catalog (id, description)
VALUES (8, 'National');
INSERT INTO public.npi_catalog (id, description)
VALUES (9, 'Private');
INSERT INTO public.npi_catalog (id, description)
VALUES (10, 'ERMES');
INSERT INTO public.npi_catalog (id, description)
VALUES (14, 'Internet (IP)');
INSERT INTO public.npi_catalog (id, description)
VALUES (18, 'WAP');


--
-- Data for Name: numbering_plan; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.numbering_plan (id, name)
VALUES (-1, 'UNDEFINED');
INSERT INTO public.numbering_plan (id, name)
VALUES (0, 'unknown');
INSERT INTO public.numbering_plan (id, name)
VALUES (1, 'ISDN');
INSERT INTO public.numbering_plan (id, name)
VALUES (2, 'spare_2');
INSERT INTO public.numbering_plan (id, name)
VALUES (3, 'data');
INSERT INTO public.numbering_plan (id, name)
VALUES (4, 'telex');
INSERT INTO public.numbering_plan (id, name)
VALUES (5, 'spare_5');
INSERT INTO public.numbering_plan (id, name)
VALUES (6, 'land_mobile');
INSERT INTO public.numbering_plan (id, name)
VALUES (7, 'spare_7');
INSERT INTO public.numbering_plan (id, name)
VALUES (8, 'national');
INSERT INTO public.numbering_plan (id, name)
VALUES (9, 'private_plan');
INSERT INTO public.numbering_plan (id, name)
VALUES (15, 'reserved');


--
-- Data for Name: operator_mno; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: origination_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.origination_type (id, name)
VALUES (1, 'All');
INSERT INTO public.origination_type (id, name)
VALUES (2, 'LocalOriginated');
INSERT INTO public.origination_type (id, name)
VALUES (3, 'RemoteOriginated');


--
-- Data for Name: report_file; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: routing_rules; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: routing_rules_action_advanced; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: routing_rules_destination; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: rule_type; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.rule_type (id, name)
VALUES (1, 'Solitary');
INSERT INTO public.rule_type (id, name)
VALUES (2, 'Dominant');
INSERT INTO public.rule_type (id, name)
VALUES (3, 'Loadshared');
INSERT INTO public.rule_type (id, name)
VALUES (4, 'Broadcast');


--
-- Data for Name: sccp; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sccp_addresses; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sccp_mtp3_destinations; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sccp_remote_resources; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sccp_rules; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sccp_service_access_points; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sequence_networks_id; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: service_provider; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: sls_range; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sls_range (id, name)
VALUES ('All', 'All');
INSERT INTO public.sls_range (id, name)
VALUES ('Odd', 'Odd');
INSERT INTO public.sls_range (id, name)
VALUES ('Even', 'Even');


--
-- Data for Name: smpp_server; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.smpp_server (id, created_at, created_by_id, updated_at, updated_by_id, action_status, enabled, ip,
                                is_default, name, port, processor_degree, queue_capacity, status, transaction_timer,
                                wait_for_bind)
VALUES (1, '2025-09-11 12:53:41.853974', NULL, '2025-09-11 12:53:41.853974', NULL, '', 1, '127.0.0.1', true,
        'Default smpp server', 2776, 15, 1000, 'STOPPED', 5000, 5000);


--
-- Data for Name: ss7_gateways; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: tcap; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Data for Name: ton_catalog; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.ton_catalog (id, description)
VALUES (-1, 'Default');
INSERT INTO public.ton_catalog (id, description)
VALUES (0, 'Unknown');
INSERT INTO public.ton_catalog (id, description)
VALUES (1, 'International');
INSERT INTO public.ton_catalog (id, description)
VALUES (2, 'National');
INSERT INTO public.ton_catalog (id, description)
VALUES (3, 'Network Specific');
INSERT INTO public.ton_catalog (id, description)
VALUES (4, 'Subscriber Number');
INSERT INTO public.ton_catalog (id, description)
VALUES (5, 'Alphanumeric');
INSERT INTO public.ton_catalog (id, description)
VALUES (6, 'Abbreviated');


--
-- Data for Name: traffic_mode; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.traffic_mode (id, name)
VALUES (1, 'Override');
INSERT INTO public.traffic_mode (id, name)
VALUES (2, 'Loadshare');
INSERT INTO public.traffic_mode (id, name)
VALUES (3, 'Broadcast');


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.user_roles (user_id, role)
VALUES (1, 'ROOT');


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users (id, created_at, created_by_id, updated_at, updated_by_id, account_locked,
                          failed_login_attempts, jwt, last_failed_login_time, last_name, lock_time, login,
                          must_change_password, name, password, status, user_name)
VALUES (1, '2025-09-11 12:53:40.620454', NULL, '2025-09-11 12:53:40.620454', NULL, false, 0, NULL, NULL, 'ROOT', NULL,
        false, true, 'ROOT', '$2a$10$25UeNP/7PRYe40UnsichSedWfV/bs9vcKBTxoxHDEkIarQkqUXEza', 1, 'admin');

---------- INSERTS AND SEQUENCES SETTING ----------
SELECT pg_catalog.setval('public.broadcast_file_id_seq', 1, false);

SELECT pg_catalog.setval('public.broadcast_id_seq', 1, false);

SELECT pg_catalog.setval('public.callback_header_http_id_seq', 1, false);

SELECT pg_catalog.setval('public.credit_sales_history_id_seq', 1, false);

SELECT pg_catalog.setval('public.custom_param_matcher_id_seq', 1, false);

SELECT pg_catalog.setval('public.delivery_error_code_id_seq', 1, false);

SELECT pg_catalog.setval('public.diameter_application_id_seq', 1, false);

SELECT pg_catalog.setval('public.diameter_gateway_id_seq', 1, false);

SELECT pg_catalog.setval('public.diameter_local_peer_id_seq', 1, false);

SELECT pg_catalog.setval('public.diameter_parameters_id_seq', 1, false);

SELECT pg_catalog.setval('public.diameter_peer_id_seq', 1, false);

SELECT pg_catalog.setval('public.diameter_realm_id_seq', 1, false);

SELECT pg_catalog.setval('public.dnd_entry_list_id_seq', 1, false);

SELECT pg_catalog.setval('public.dnd_entry_msidn_id_seq', 1, false);

SELECT pg_catalog.setval('public.error_code_id_seq', 1, false);

SELECT pg_catalog.setval('public.error_code_mapping_id_seq', 1, false);

SELECT pg_catalog.setval('public.interpreter_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_app_servers_routes_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_application_server_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_ass_app_servers_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_associations_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_routes_id_seq', 1, false);

SELECT pg_catalog.setval('public.m3ua_sockets_id_seq', 1, false);

SELECT pg_catalog.setval('public.map_id_seq', 1, false);

SELECT pg_catalog.setval('public.networks_id_seq', 1, false);

SELECT pg_catalog.setval('public.operator_mno_id_seq', 1, false);

SELECT pg_catalog.setval('public.report_file_id_seq', 1, false);

SELECT pg_catalog.setval('public.routing_rules_destination_id_seq', 1, false);

SELECT pg_catalog.setval('public.routing_rules_id_seq', 1, false);

SELECT pg_catalog.setval('public.sccp_addresses_id_seq', 1, false);

SELECT pg_catalog.setval('public.sccp_id_seq', 1, false);

SELECT pg_catalog.setval('public.sccp_mtp3_destinations_id_seq', 1, false);

SELECT pg_catalog.setval('public.sccp_remote_resources_id_seq', 1, false);

SELECT pg_catalog.setval('public.sccp_rules_id_seq', 1, false);

SELECT pg_catalog.setval('public.sccp_service_access_points_id_seq', 1, false);

SELECT pg_catalog.setval('public.smpp_server_id_seq', 33, true);

SELECT pg_catalog.setval('public.tcap_id_seq', 1, false);

SELECT pg_catalog.setval('public.user_code_id_seq', 33, true);

ALTER TABLE ONLY public.balance_type
    ADD CONSTRAINT balance_type_pkey PRIMARY KEY (name);

ALTER TABLE ONLY public.bind_statuses
    ADD CONSTRAINT bind_statuses_pkey PRIMARY KEY (state);

ALTER TABLE ONLY public.binds_types
    ADD CONSTRAINT binds_types_pkey PRIMARY KEY (_type);

ALTER TABLE ONLY public.broadcast_devices
    ADD CONSTRAINT broadcast_devices_pkey PRIMARY KEY (message_id);

ALTER TABLE ONLY public.broadcast_file
    ADD CONSTRAINT broadcast_file_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.broadcast
    ADD CONSTRAINT broadcast_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.callback_header_http
    ADD CONSTRAINT callback_header_http_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.common_variables
    ADD CONSTRAINT common_variables_pkey PRIMARY KEY (key);

ALTER TABLE ONLY public.credit_sales_history
    ADD CONSTRAINT credit_sales_history_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.custom_param_matcher
    ADD CONSTRAINT custom_param_matcher_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.delivery_error_code
    ADD CONSTRAINT delivery_error_code_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.delivery_status
    ADD CONSTRAINT delivery_status_pkey PRIMARY KEY (value);

ALTER TABLE ONLY public.diameter_application
    ADD CONSTRAINT diameter_application_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.diameter_gateway
    ADD CONSTRAINT diameter_gateway_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.diameter_local_peer
    ADD CONSTRAINT diameter_local_peer_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.diameter_parameters
    ADD CONSTRAINT diameter_parameters_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.diameter_peer
    ADD CONSTRAINT diameter_peer_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.diameter_realm
    ADD CONSTRAINT diameter_realm_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dnd_entry_list
    ADD CONSTRAINT dnd_entry_list_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dnd_entry_msidn
    ADD CONSTRAINT dnd_entry_msidn_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.encoding_type
    ADD CONSTRAINT encoding_type_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT error_code_mapping_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.error_code
    ADD CONSTRAINT error_code_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.functionality
    ADD CONSTRAINT functionality_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT gateways_pkey PRIMARY KEY (network_id);

ALTER TABLE ONLY public.general_settings_smpp_http
    ADD CONSTRAINT general_settings_smpp_http_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.general_smsc_retry
    ADD CONSTRAINT general_smsc_retry_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.global_title_indicator
    ADD CONSTRAINT global_title_indicator_pkey PRIMARY KEY (gt_indicator_id);

ALTER TABLE ONLY public.interfaz_versions
    ADD CONSTRAINT interfaz_versions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.interpreter
    ADD CONSTRAINT interpreter_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.load_sharing_algorithm
    ADD CONSTRAINT load_sharing_algorithm_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua_app_servers_routes
    ADD CONSTRAINT m3ua_app_servers_routes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua_application_server
    ADD CONSTRAINT m3ua_application_server_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua_ass_app_servers
    ADD CONSTRAINT m3ua_ass_app_servers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua_associations
    ADD CONSTRAINT m3ua_associations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua
    ADD CONSTRAINT m3ua_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua_routes
    ADD CONSTRAINT m3ua_routes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.m3ua_sockets
    ADD CONSTRAINT m3ua_sockets_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.map
    ADD CONSTRAINT map_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.nature_of_address
    ADD CONSTRAINT nature_of_address_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.npi_catalog
    ADD CONSTRAINT npi_catalog_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.numbering_plan
    ADD CONSTRAINT numbering_plan_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.operator_mno
    ADD CONSTRAINT operator_mno_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.origination_type
    ADD CONSTRAINT origination_type_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.report_file
    ADD CONSTRAINT report_file_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.routing_rules_action_advanced
    ADD CONSTRAINT routing_rules_action_advanced_pkey PRIMARY KEY (routing_rules_id);

ALTER TABLE ONLY public.routing_rules_destination
    ADD CONSTRAINT routing_rules_destination_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT routing_rules_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.rule_type
    ADD CONSTRAINT rule_type_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sccp_addresses
    ADD CONSTRAINT sccp_addresses_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sccp_mtp3_destinations
    ADD CONSTRAINT sccp_mtp3_destinations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sccp
    ADD CONSTRAINT sccp_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sccp_remote_resources
    ADD CONSTRAINT sccp_remote_resources_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT sccp_rules_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sccp_service_access_points
    ADD CONSTRAINT sccp_service_access_points_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sequence_networks_id
    ADD CONSTRAINT sequence_networks_id_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT service_provider_pkey PRIMARY KEY (network_id);

ALTER TABLE ONLY public.sls_range
    ADD CONSTRAINT sls_range_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.smpp_server
    ADD CONSTRAINT smpp_server_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT ss7_gateways_pkey PRIMARY KEY (network_id);

ALTER TABLE ONLY public.tcap
    ADD CONSTRAINT tcap_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ton_catalog
    ADD CONSTRAINT ton_catalog_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.traffic_mode
    ADD CONSTRAINT traffic_mode_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.error_code
    ADD CONSTRAINT uk15bb2qv9ktfalxfpitj5x58r7 UNIQUE (code, mno_id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT uk4v8vwdmj5kibt2iw19asg2lhy UNIQUE (error_code_id, delivery_error_code_id);

ALTER TABLE ONLY public.dnd_entry_msidn
    ADD CONSTRAINT uk50tyoxhss8t0upsgiooe1jp10 UNIQUE (msisdn, parent_id);

ALTER TABLE ONLY public.sccp
    ADD CONSTRAINT uk_1hs9t4qrthn4pjna69kp7a40u UNIQUE (external_id);

ALTER TABLE ONLY public.map
    ADD CONSTRAINT uk_1rwmxepoi4wmkchj7ap2bc170 UNIQUE (network_id);

ALTER TABLE ONLY public.tcap
    ADD CONSTRAINT uk_50g747kv9i16a2i6p0qey7yfk UNIQUE (external_id);

ALTER TABLE ONLY public.m3ua
    ADD CONSTRAINT uk_5gyru34hwrfrvus7qpeip766a UNIQUE (network_id);

ALTER TABLE ONLY public.m3ua
    ADD CONSTRAINT uk_72w8mc6t4qgeyijb3ihhyx3q9 UNIQUE (external_id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT uk_8g3p6td80453eqio5d3klg70d UNIQUE (external_id);

ALTER TABLE ONLY public.smpp_server
    ADD CONSTRAINT uk_f5o79sv57merpxtkj3rp29nw1 UNIQUE (name);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT uk_f6bf4balmalqikofvuj9k0jb6 UNIQUE (external_id);

ALTER TABLE ONLY public.tcap
    ADD CONSTRAINT uk_fcqyk51umykvg9cxbe878nwg4 UNIQUE (network_id);

ALTER TABLE ONLY public.operator_mno
    ADD CONSTRAINT uk_grjbc6prffbq21t89uoc5q7ab UNIQUE (name);

ALTER TABLE ONLY public.delivery_error_code
    ADD CONSTRAINT uk_hpm0cwkywc6bh457qribqr4p7 UNIQUE (code);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT uk_itno4ind86g2ks7pc529ta4bo UNIQUE (external_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_k8d0f2n7n88w1a16yhua64onx UNIQUE (user_name);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT uk_m6pnr7is7owjjbmcfqbic3vpq UNIQUE (name);

ALTER TABLE ONLY public.map
    ADD CONSTRAINT uk_q76dlitcleejpp7ds3qoky2bw UNIQUE (external_id);

ALTER TABLE ONLY public.sccp
    ADD CONSTRAINT uk_r0porb4w9l2vklk572mf8ghuo UNIQUE (network_id);

ALTER TABLE ONLY public.m3ua_app_servers_routes
    ADD CONSTRAINT ukebqqhwax6g5205v6k3nh4ogv5 UNIQUE (route_id, application_server_id);

ALTER TABLE ONLY public.smpp_server
    ADD CONSTRAINT ukftxjkkockmiwbc4l4im7rpuok UNIQUE (ip, port);

ALTER TABLE ONLY public.m3ua_routes
    ADD CONSTRAINT ukgyalwjug4ny0f9ctleav2krsq UNIQUE (m3ua_id, origination_point_code, destination_point_code, service_indicator);

ALTER TABLE ONLY public.dnd_entry_list
    ADD CONSTRAINT ukjo54hta4g8c1qvibtwt43qqy3 UNIQUE (name);

ALTER TABLE ONLY public.interpreter
    ADD CONSTRAINT ukm0ck5fql1xp53seqfyc17d343 UNIQUE (event_type, direction, gateway_id);

ALTER TABLE ONLY public.m3ua_ass_app_servers
    ADD CONSTRAINT ukmd89q1mobwwfpckj8rcpm3h7p UNIQUE (asp_id, application_server_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: user_service_provider; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_service_provider
(
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER NOT NULL,
    service_provider_id INTEGER NOT NULL,
    CONSTRAINT uk_user_service_provider UNIQUE (user_id, service_provider_id),
    CONSTRAINT fk_user_service_provider_user FOREIGN KEY (user_id) REFERENCES public.users(id),
    CONSTRAINT fk_user_service_provider_sp FOREIGN KEY (service_provider_id) REFERENCES public.service_provider(network_id)
);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fk1hr3w93a1nq7srmhqebwo3bp3 FOREIGN KEY (bind_type) REFERENCES public.binds_types(_type);

ALTER TABLE ONLY public.dnd_entry_list
    ADD CONSTRAINT fk28vhrbkw0jys13tyroicj0f1c FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT fk2we8j4idp8xbdjjajaxg0muiu FOREIGN KEY (error_code_id) REFERENCES public.error_code(id);

ALTER TABLE ONLY public.smpp_server
    ADD CONSTRAINT fk3965hbs8eaqwebkq38eo9xedc FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.routing_rules_destination
    ADD CONSTRAINT fk3ensnwtyfhohht16gardgcrn3 FOREIGN KEY (network_id) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.diameter_application
    ADD CONSTRAINT fk43cn13mk4v96qmmthwnu7agad FOREIGN KEY (diameter_local_peer_id) REFERENCES public.diameter_local_peer(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fk44prj5j85dguvrnolnsref45x FOREIGN KEY (status) REFERENCES public.bind_statuses(state);

ALTER TABLE ONLY public.diameter_gateway
    ADD CONSTRAINT fk48l192f009v7941lf8t1klt15 FOREIGN KEY (mno_id) REFERENCES public.operator_mno(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fk4gmn1wynsdo7tw9wmiqf3k2g0 FOREIGN KEY (address_ton) REFERENCES public.ton_catalog(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fk5d46cpsyvq0vk2renvj4ultoy FOREIGN KEY (encoding_gsm7) REFERENCES public.encoding_type(id);

ALTER TABLE ONLY public.credit_sales_history
    ADD CONSTRAINT fk5qduc30gwf8o0c84hctlccpgv FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.general_settings_smpp_http
    ADD CONSTRAINT fk62bsxkem9cnnfcyjc20ttvalp FOREIGN KEY (encoding_gsm7) REFERENCES public.encoding_type(id);

ALTER TABLE ONLY public.sccp_service_access_points
    ADD CONSTRAINT fk6cjknd0shucvcemy7dqc8o0b1 FOREIGN KEY (ss7_sccp_id) REFERENCES public.sccp(id);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT fk6ee1kvbwoy8w0uaplhepa6wbx FOREIGN KEY (network_id) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fk6hy1e9qtfsa0gr2994mnu09si FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fk6jtbtfos6leyhjdgj49l4tvt9 FOREIGN KEY (smpp_server_id) REFERENCES public.smpp_server(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fk6ma0f6uk7bv59nxixtgnctuok FOREIGN KEY (interface_version) REFERENCES public.interfaz_versions(id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk6nm9u1qpw9xxphk70xr614m7n FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fk6soe35of9gh35sco3abb7k661 FOREIGN KEY (new_dest_addr_npi) REFERENCES public.npi_catalog(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fk70y9wv9t3sysh1ev8onthxxrs FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fk72e5dtxmgl4oasa4q05wrrfds FOREIGN KEY (address_npi) REFERENCES public.npi_catalog(id);

ALTER TABLE ONLY public.diameter_peer
    ADD CONSTRAINT fk7cxs7ugi985qfs35bd1nrhxjq FOREIGN KEY (diameter_gateway_id) REFERENCES public.diameter_gateway(id);

--ALTER TABLE ONLY public.dnd_entry_msidn
--ADD CONSTRAINT fk7pughay5b59y55ls89k7xbfsi FOREIGN KEY (parent_id) REFERENCES public.dnd_entry_list (id);

ALTER TABLE ONLY public.delivery_error_code
    ADD CONSTRAINT fk82mxdl1clh7wynqn52qyhp260 FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fk86un5bs185j4py6ld0hmm36a3 FOREIGN KEY (rule_type_id) REFERENCES public.rule_type(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fk8e9ba9lhkkiagdb1qvj59t6mf FOREIGN KEY (address_ton) REFERENCES public.ton_catalog(id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk8nakkftyppd62ke6tv7oo5a92 FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fk8ywuwyn0e95p6budw28md2v9q FOREIGN KEY (new_dest_addr_ton) REFERENCES public.ton_catalog(id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT fk918xa3bh75vo327bv4g6j078l FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT fk945oyjc2n6yp12kvgcbx17sxc FOREIGN KEY (delivery_error_code_id) REFERENCES public.delivery_error_code(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fk971cp4mn25v74d68u8jueaqn FOREIGN KEY (load_sharing_algorithm_id) REFERENCES public.load_sharing_algorithm(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fk9aq6pvmroxx2jp4wrfljtfge8 FOREIGN KEY (origination_type_id) REFERENCES public.origination_type(id);

ALTER TABLE ONLY public.general_settings_smpp_http
    ADD CONSTRAINT fk9phdtwk9196ywhlxwq6n12ak7 FOREIGN KEY (encoding_iso88591) REFERENCES public.encoding_type(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fk9w3j7oj5fm97yeqm0euf3asjy FOREIGN KEY (new_source_addr_ton) REFERENCES public.ton_catalog(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fka35h5i1rs9v2bperkxh2vw7vs FOREIGN KEY (network_id_to_permanent_failure) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.broadcast
    ADD CONSTRAINT fkaeipqo1wnho85fi22yvrvratl FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkafcy4fm53nealuu6ngo5e1sdv FOREIGN KEY (address_npi) REFERENCES public.npi_catalog(id);

ALTER TABLE ONLY public.sccp_addresses
    ADD CONSTRAINT fkajr598l3ib742nyfa8wg5kx06 FOREIGN KEY (nature_of_address_id) REFERENCES public.nature_of_address(id);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT fkamb81g63dtvvv43vahasht64m FOREIGN KEY (global_title_indicator) REFERENCES public.global_title_indicator(gt_indicator_id);

ALTER TABLE ONLY public.sccp_addresses
    ADD CONSTRAINT fkaphrbjdtkfeb9sd0w9sp88tvc FOREIGN KEY (ss7_sccp_id) REFERENCES public.sccp(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fkbb8f91tf8dsnxt6g4uaqupms8 FOREIGN KEY (balance_type) REFERENCES public.balance_type(name);

ALTER TABLE ONLY public.diameter_gateway
    ADD CONSTRAINT fkbcjhu0lf29130ut3p26aeffi0 FOREIGN KEY (diameter_parameters_id) REFERENCES public.diameter_parameters(id);

ALTER TABLE ONLY public.diameter_gateway
    ADD CONSTRAINT fkbhlvuhy013bx2dhv39kpjgddh FOREIGN KEY (diameter_local_peer_id) REFERENCES public.diameter_local_peer(id);

ALTER TABLE ONLY public.m3ua_ass_app_servers
    ADD CONSTRAINT fkboq2hgut7uso5aqrvocu0199s FOREIGN KEY (asp_id) REFERENCES public.m3ua_associations(id);

ALTER TABLE ONLY public.tcap
    ADD CONSTRAINT fkbqi8vmhhfy2jvu9d6bc0fye12 FOREIGN KEY (network_id) REFERENCES public.ss7_gateways(network_id);

ALTER TABLE ONLY public.broadcast
    ADD CONSTRAINT fkbumghnfi5kattl0x1fxjr5nt0 FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fkbxsovx8b75kldkylerv0sm0wn FOREIGN KEY (network_id_to_map_sri) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.m3ua_sockets
    ADD CONSTRAINT fkc3lkb2mbueteg4af7y4j5cb99 FOREIGN KEY (ss7_m3ua_id) REFERENCES public.m3ua(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkc57rphbixo3h1ade0xj8ptbb6 FOREIGN KEY (encoding_ucs2) REFERENCES public.encoding_type(id);

ALTER TABLE ONLY public.credit_sales_history
    ADD CONSTRAINT fkc78whab4gorymw2ggs78fhvyd FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.m3ua_application_server
    ADD CONSTRAINT fkca0mtgjxb1w3dni3mci1yaoyo FOREIGN KEY (traffic_mode_id) REFERENCES public.traffic_mode(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fkcky69a4h9d4yq0920sqfrxe8t FOREIGN KEY (network_id_temp_failure) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.diameter_realm
    ADD CONSTRAINT fkcp2g65ukw4ihknqpta8506t5c FOREIGN KEY (diameter_gateway_id) REFERENCES public.diameter_gateway(id);

ALTER TABLE ONLY public.routing_rules_destination
    ADD CONSTRAINT fkcus6n2dflfexfmmstumie14m0 FOREIGN KEY (routing_rules_id) REFERENCES public.routing_rules(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fkcv4gi3o8dprt103fpnub0bytw FOREIGN KEY (secondary_address_id) REFERENCES public.sccp_addresses(id);

ALTER TABLE ONLY public.sccp_remote_resources
    ADD CONSTRAINT fkdlfbwrrb24se7hdsv94huwe7i FOREIGN KEY (ss7_sccp_id) REFERENCES public.sccp(id);

ALTER TABLE ONLY public.general_settings_smpp_http
    ADD CONSTRAINT fkdob2n4cx5pnlt6bk0xsqsqhwr FOREIGN KEY (encoding_ucs2) REFERENCES public.encoding_type(id);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT fke398mjq1j5s3sdtrtnebwcdhl FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.error_code
    ADD CONSTRAINT fkehkcnny5wavo8tvh5sl9d9iom FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.m3ua_application_server
    ADD CONSTRAINT fkfj181ikvmmcb1hdw3lrapxdwe FOREIGN KEY (functionality) REFERENCES public.functionality(id);

ALTER TABLE ONLY public.routing_rules_action_advanced
    ADD CONSTRAINT fkfl0tjlpwmaaxqscsy78i0gxw1 FOREIGN KEY (routing_rules_id) REFERENCES public.routing_rules(id);

ALTER TABLE ONLY public.credit_sales_history
    ADD CONSTRAINT fkg5m57ufr6t67rmh4c5pgdjipl FOREIGN KEY (network_id) REFERENCES public.service_provider(network_id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fkgp4dyceoegce1joqroq4hxf86 FOREIGN KEY (numbering_plan_id) REFERENCES public.numbering_plan(id);

ALTER TABLE ONLY public.interpreter
    ADD CONSTRAINT fkh0a61qpvpf574q67bj58xkh69 FOREIGN KEY (gateway_id) REFERENCES public.gateways(network_id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkh2lo1hgy0juulwpamegp0e41v FOREIGN KEY (interface_version) REFERENCES public.interfaz_versions(id);

ALTER TABLE ONLY public.operator_mno
    ADD CONSTRAINT fkh31cq7s2c2l3jj6n94i44x7wd FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.m3ua_routes
    ADD CONSTRAINT fkh5acdxuunq5xgabs9g4j1aea9 FOREIGN KEY (traffic_mode_id) REFERENCES public.traffic_mode(id);

ALTER TABLE ONLY public.report_file
    ADD CONSTRAINT fkh6aqscvfbcfu0rkf1uu3wifro FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fkhg3nwsyatxey2tamhednigjh5 FOREIGN KEY (origin_network_id) REFERENCES public.sequence_networks_id(id);

--ALTER TABLE ONLY public.broadcast ADD CONSTRAINT fki1718e1n8goy95niagi31p4l3 FOREIGN KEY (file_id) REFERENCES public.broadcast_file (id);

ALTER TABLE ONLY public.sccp
    ADD CONSTRAINT fki502jqvnmt4aa9lk7wdb4e80t FOREIGN KEY (network_id) REFERENCES public.ss7_gateways(network_id);

ALTER TABLE ONLY public.dnd_entry_list
    ADD CONSTRAINT fki7bc16e0vn0u4jdu11gnm1ffb FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fkjpeyrux99m5i6p8u2q4gms79 FOREIGN KEY (primary_address_id) REFERENCES public.sccp_addresses(id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT fkk1df61wbr9lj066kh97x3txbl FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.operator_mno
    ADD CONSTRAINT fkkbghhekhpgu3bl0noand2wolq FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fkll9kgjob0h2148nqdjyoofept FOREIGN KEY (bind_type) REFERENCES public.binds_types(_type);

ALTER TABLE ONLY public.error_code
    ADD CONSTRAINT fklreaj5j5e3gic3078qg05ikuy FOREIGN KEY (mno_id) REFERENCES public.operator_mno(id);

ALTER TABLE ONLY public.tcap
    ADD CONSTRAINT fkm0giwue0dwid958e7qlpi2qt8 FOREIGN KEY (sls_range_id) REFERENCES public.sls_range(id);

ALTER TABLE ONLY public.smpp_server
    ADD CONSTRAINT fkm3ui3iesfffn3sempjxb3jhda FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkmdyrp5w7ky69i9h48k7imay6i FOREIGN KEY (network_id) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT fkmpuvx32oewf6d9qh5cd9dv5f4 FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.m3ua_ass_app_servers
    ADD CONSTRAINT fkmr9a2epwq9m0bv3u2cpde8b0j FOREIGN KEY (application_server_id) REFERENCES public.m3ua_application_server(id);

ALTER TABLE ONLY public.m3ua_associations
    ADD CONSTRAINT fkn9f0dqurj9pjimyfbkijyhkd1 FOREIGN KEY (m3ua_socket_id) REFERENCES public.m3ua_sockets(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fknpjfcqe0d91lwao1v8ryu5ui2 FOREIGN KEY (nature_of_address_id) REFERENCES public.nature_of_address(id);

ALTER TABLE ONLY public.diameter_gateway
    ADD CONSTRAINT fknwird38qb0oigl7ygq2xkj3dl FOREIGN KEY (network_id) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fko5i3ogd2379v2rs0uv67y2wgj FOREIGN KEY (network_id) REFERENCES public.sequence_networks_id(id);

ALTER TABLE ONLY public.delivery_error_code
    ADD CONSTRAINT fko62geb4dkg6yajp1ixm3q72h9 FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.error_code_mapping
    ADD CONSTRAINT fkojoedwssx6jilchx3tkhahqsx FOREIGN KEY (delivery_status_id) REFERENCES public.delivery_status(value);

ALTER TABLE ONLY public.m3ua_app_servers_routes
    ADD CONSTRAINT fkop7ko7rwoucyf9oybppggii4v FOREIGN KEY (application_server_id) REFERENCES public.m3ua_application_server(id);

ALTER TABLE ONLY public.m3ua
    ADD CONSTRAINT fkoxu3uwdhl9ufmrx8xh0wj7kco FOREIGN KEY (network_id) REFERENCES public.ss7_gateways(network_id);

--ALTER TABLE ONLY public.gateways ADD CONSTRAINT fkp3fy2c2ba20faukx1haf2rab5 FOREIGN KEY (mno_id) REFERENCES public.operator_mno(id);

ALTER TABLE ONLY public.ss7_gateways
    ADD CONSTRAINT fkpe3wpm3d1gva1hcmduaxmjexs FOREIGN KEY (mno_id) REFERENCES public.operator_mno(id);

ALTER TABLE ONLY public.m3ua_app_servers_routes
    ADD CONSTRAINT fkq1g2prfrlsteb2npwurmsmwxc FOREIGN KEY (route_id) REFERENCES public.m3ua_routes(id);

ALTER TABLE ONLY public.diameter_realm
    ADD CONSTRAINT fkq8xlvd23jh6nth4edtti0joid FOREIGN KEY (diameter_application_id) REFERENCES public.diameter_application(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkqbhoblk72p8hhccwmhvcoajw1 FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.map
    ADD CONSTRAINT fkqj3226nferd1nk5rng5qki9oa FOREIGN KEY (network_id) REFERENCES public.ss7_gateways(network_id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fkqmf0x1xndysrdgyg86qrgatjt FOREIGN KEY (calling_nature_of_address_id) REFERENCES public.nature_of_address(id);

ALTER TABLE ONLY public.error_code
    ADD CONSTRAINT fkqmsok2xjh5esavnr6c7ojphdb FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.sccp_rules
    ADD CONSTRAINT fkr66d786woca1eaa25ecy43onk FOREIGN KEY (calling_numbering_plan_id) REFERENCES public.numbering_plan(id);

ALTER TABLE ONLY public.sccp_addresses
    ADD CONSTRAINT fkrwhheucorf0e1myip2kusi0ex FOREIGN KEY (numbering_plan_id) REFERENCES public.numbering_plan(id);

ALTER TABLE ONLY public.broadcast_devices
    ADD CONSTRAINT fks7iqsqqsudlhoyfnubdis8mpy FOREIGN KEY (broadcast_id) REFERENCES public.broadcast(id);

ALTER TABLE ONLY public.service_provider
    ADD CONSTRAINT fksc22owlnmdic39itv8yqpy7xf FOREIGN KEY (created_by_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT fksj71vtk8uevl1htib8xhmi3pc FOREIGN KEY (new_source_addr_npi) REFERENCES public.npi_catalog(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkst7s9ydlycyps8oedxkf08po0 FOREIGN KEY (status) REFERENCES public.bind_statuses(state);

ALTER TABLE ONLY public.sccp_mtp3_destinations
    ADD CONSTRAINT fkstbbmxonimogy3avxjga1gxos FOREIGN KEY (sccp_sap_id) REFERENCES public.sccp_service_access_points(id);

ALTER TABLE ONLY public.gateways
    ADD CONSTRAINT fkthmdt811l9lleiqshh7exj35b FOREIGN KEY (encoding_iso88591) REFERENCES public.encoding_type(id);

ALTER TABLE ONLY public.custom_param_matcher
    ADD CONSTRAINT fktihfrm38iem4ayxkqhpq80cc3 FOREIGN KEY (routing_rule_id) REFERENCES public.routing_rules(id);


CREATE TABLE cdr
(
    id                         SERIAL    NOT NULL,
    record_date                TIMESTAMP NOT NULL,
    submit_date                TIMESTAMP,
    delivery_date              TIMESTAMP,
    message_type               VARCHAR(10),
    message_id                 VARCHAR(50),
    origination_protocol       VARCHAR(20),
    origination_network_id     VARCHAR(20),
    origination_type           VARCHAR(5),
    destination_type           VARCHAR(20),
    destination_protocol       VARCHAR(20),
    destination_network_id     VARCHAR(20),
    routing_id                 INT,
    status                     VARCHAR(50),
    status_code                VARCHAR(50),
    comment                    TEXT,
    dialog_duration            BIGINT,
    processing_time            BIGINT,
    data_coding                INT,
    validity_period            VARCHAR(50),
    addr_src_digits            VARCHAR(50),
    addr_src_ton               INT,
    addr_src_npi               INT,
    addr_dst_digits            VARCHAR(50),
    addr_dst_ton               INT,
    addr_dst_npi               INT,
    remote_dialog_id           BIGINT,
    local_dialog_id            BIGINT,
    local_spc                  INT,
    local_ssn                  INT,
    local_global_title_digits  VARCHAR(255),
    remote_spc                 INT,
    remote_ssn                 INT,
    remote_global_title_digits VARCHAR(50),
    imsi                       VARCHAR(50),
    nnn_digits                 VARCHAR(50),
    originator_sccp_address    VARCHAR(50),
    mt_service_center_address  VARCHAR(50),
    message                    TEXT,
    esm_class                  VARCHAR(20),
    udhi                       VARCHAR(20),
    registered_delivery        INT,
    msg_reference_number       VARCHAR(20),
    total_segment              INT,
    segment_sequence           INT,
    retry_number               INT,
    parent_id                  VARCHAR(50),
    broadcast_id               INT,
    mno_message_id             varchar(50),
    PRIMARY KEY
        (
         id,
         record_date
            )
);

INSERT INTO cdr (record_date, submit_date, delivery_date, message_type, message_id, origination_protocol,
                 origination_network_id, origination_type, destination_type, destination_protocol,
                 destination_network_id, routing_id, status, status_code, comment, dialog_duration, processing_time,
                 data_coding, validity_period, addr_src_digits, addr_src_ton, addr_src_npi, addr_dst_digits,
                 addr_dst_ton, addr_dst_npi, remote_dialog_id, local_dialog_id, local_spc, local_ssn,
                 local_global_title_digits, remote_spc, remote_ssn, remote_global_title_digits, imsi, nnn_digits,
                 originator_sccp_address, mt_service_center_address, message, esm_class, udhi, registered_delivery,
                 msg_reference_number, total_segment, segment_sequence, retry_number, parent_id, broadcast_id,
                 mno_message_id)
VALUES ('2025-06-04 00:00:00', '2025-06-04 00:00:00', '2025-06-04 00:00:00', 'MO', 'msgid12345', 'SMPP', '1', '0',
        'International', 'SMPP', '1', 1, 'SUCCESS', '4', 'OK', 5000, 2000, 8,
        '1 day', '1234567890', 1, 1, '0987654321', 1, 1, 123456789012345, 678901234567890, 1001, 2001,
        '1234567890123456789', 3001, 4001, '0987654321098765432', '310150123456789', '123456', '1234567890',
        '0987654321', 'Hello.', 3, '0', 1, 'ref12345', 1, 1, 0, NULL,
        NULL, 'mno_msgid12345');

CREATE TABLE cdr_status_code
(
    status_code VARCHAR(50),
    status      VARCHAR(50),
    protocol    VARCHAR(50),
    PRIMARY KEY (status_code, protocol)
);

INSERT INTO cdr_status_code (status_code, status, protocol)
VALUES
    -- EXPIRED SMPP
    ('98', 'EXPIRED', 'SMPP'),

    -- UNDELIVERED SMPP
    ('4', 'UNDELIVERED', 'SMPP'),
    ('5', 'UNDELIVERED', 'SMPP'),
    ('20', 'UNDELIVERED', 'SMPP'),
    ('88', 'UNDELIVERED', 'SMPP'),
    ('99', 'UNDELIVERED', 'SMPP'),
    ('195', 'UNDELIVERED', 'SMPP'),
    ('254', 'UNDELIVERED', 'SMPP'),
    ('255', 'UNDELIVERED', 'SMPP'),
    ('258', 'UNDELIVERED', 'SMPP'),
    ('300', 'UNDELIVERED', 'SMPP'),
    ('504', 'UNDELIVERED', 'SMPP'),
    ('506', 'UNDELIVERED', 'SMPP'),

    -- REJECTED SMPP
    ('1', 'REJECTED', 'SMPP'),
    ('2', 'REJECTED', 'SMPP'),
    ('3', 'REJECTED', 'SMPP'),
    ('6', 'REJECTED', 'SMPP'),
    ('7', 'REJECTED', 'SMPP'),
    ('8', 'REJECTED', 'SMPP'),
    ('10', 'REJECTED', 'SMPP'),
    ('11', 'REJECTED', 'SMPP'),
    ('12', 'REJECTED', 'SMPP'),
    ('13', 'REJECTED', 'SMPP'),
    ('14', 'REJECTED', 'SMPP'),
    ('15', 'REJECTED', 'SMPP'),
    ('17', 'REJECTED', 'SMPP'),
    ('19', 'REJECTED', 'SMPP'),
    ('21', 'REJECTED', 'SMPP'),
    ('51', 'REJECTED', 'SMPP'),
    ('52', 'REJECTED', 'SMPP'),
    ('64', 'REJECTED', 'SMPP'),
    ('66', 'REJECTED', 'SMPP'),
    ('67', 'REJECTED', 'SMPP'),
    ('69', 'REJECTED', 'SMPP'),
    ('72', 'REJECTED', 'SMPP'),
    ('73', 'REJECTED', 'SMPP'),
    ('80', 'REJECTED', 'SMPP'),
    ('81', 'REJECTED', 'SMPP'),
    ('83', 'REJECTED', 'SMPP'),
    ('84', 'REJECTED', 'SMPP'),
    ('85', 'REJECTED', 'SMPP'),
    ('97', 'REJECTED', 'SMPP'),
    ('100', 'REJECTED', 'SMPP'),
    ('101', 'REJECTED', 'SMPP'),
    ('102', 'REJECTED', 'SMPP'),
    ('103', 'REJECTED', 'SMPP'),
    ('192', 'REJECTED', 'SMPP'),
    ('193', 'REJECTED', 'SMPP'),
    ('194', 'REJECTED', 'SMPP'),
    ('196', 'REJECTED', 'SMPP'),
    ('256', 'REJECTED', 'SMPP'),
    ('257', 'REJECTED', 'SMPP'),
    ('259', 'REJECTED', 'SMPP'),
    ('260', 'REJECTED', 'SMPP'),
    ('261', 'REJECTED', 'SMPP'),
    ('262', 'REJECTED', 'SMPP'),
    ('263', 'REJECTED', 'SMPP'),
    ('264', 'REJECTED', 'SMPP'),
    ('265', 'REJECTED', 'SMPP'),
    ('266', 'REJECTED', 'SMPP'),
    ('267', 'REJECTED', 'SMPP'),
    ('268', 'REJECTED', 'SMPP'),
    ('269', 'REJECTED', 'SMPP'),
    ('270', 'REJECTED', 'SMPP'),
    ('271', 'REJECTED', 'SMPP'),
    ('272', 'REJECTED', 'SMPP'),
    ('273', 'REJECTED', 'SMPP'),
    ('274', 'REJECTED', 'SMPP'),
    ('500', 'REJECTED', 'SMPP'),
    ('505', 'REJECTED', 'SMPP'),

    -- REJECTED SS7
    ('10', 'REJECTED', 'SS7'),
    ('11', 'REJECTED', 'SS7'),
    ('17', 'REJECTED', 'SS7'),
    ('23', 'REJECTED', 'SS7'),
    ('47', 'REJECTED', 'SS7'),
    ('48', 'REJECTED', 'SS7'),
    ('49', 'REJECTED', 'SS7'),
    ('52', 'REJECTED', 'SS7'),
    ('53', 'REJECTED', 'SS7'),
    ('60', 'REJECTED', 'SS7'),
    ('61', 'REJECTED', 'SS7'),
    ('500', 'REJECTED', 'SS7'),
    ('508', 'REJECTED', 'SS7'),

    -- UNDELIVERED SS7
    ('1', 'UNDELIVERED', 'SS7'),
    ('2', 'UNDELIVERED', 'SS7'),
    ('3', 'UNDELIVERED', 'SS7'),
    ('5', 'UNDELIVERED', 'SS7'),
    ('6', 'UNDELIVERED', 'SS7'),
    ('7', 'UNDELIVERED', 'SS7'),
    ('8', 'UNDELIVERED', 'SS7'),
    ('9', 'UNDELIVERED', 'SS7'),
    ('12', 'UNDELIVERED', 'SS7'),
    ('13', 'UNDELIVERED', 'SS7'),
    ('14', 'UNDELIVERED', 'SS7'),
    ('15', 'UNDELIVERED', 'SS7'),
    ('16', 'UNDELIVERED', 'SS7'),
    ('18', 'UNDELIVERED', 'SS7'),
    ('19', 'UNDELIVERED', 'SS7'),
    ('20', 'UNDELIVERED', 'SS7'),
    ('21', 'UNDELIVERED', 'SS7'),
    ('22', 'UNDELIVERED', 'SS7'),
    ('24', 'UNDELIVERED', 'SS7'),
    ('25', 'UNDELIVERED', 'SS7'),
    ('26', 'UNDELIVERED', 'SS7'),
    ('27', 'UNDELIVERED', 'SS7'),
    ('28', 'UNDELIVERED', 'SS7'),
    ('29', 'UNDELIVERED', 'SS7'),
    ('30', 'UNDELIVERED', 'SS7'),
    ('31', 'UNDELIVERED', 'SS7'),
    ('32', 'UNDELIVERED', 'SS7'),
    ('33', 'UNDELIVERED', 'SS7'),
    ('34', 'UNDELIVERED', 'SS7'),
    ('35', 'UNDELIVERED', 'SS7'),
    ('36', 'UNDELIVERED', 'SS7'),
    ('37', 'UNDELIVERED', 'SS7'),
    ('38', 'UNDELIVERED', 'SS7'),
    ('39', 'UNDELIVERED', 'SS7'),
    ('40', 'UNDELIVERED', 'SS7'),
    ('42', 'UNDELIVERED', 'SS7'),
    ('43', 'UNDELIVERED', 'SS7'),
    ('44', 'UNDELIVERED', 'SS7'),
    ('45', 'UNDELIVERED', 'SS7'),
    ('46', 'UNDELIVERED', 'SS7'),
    ('50', 'UNDELIVERED', 'SS7'),
    ('51', 'UNDELIVERED', 'SS7'),
    ('54', 'UNDELIVERED', 'SS7'),
    ('58', 'UNDELIVERED', 'SS7'),
    ('59', 'UNDELIVERED', 'SS7'),
    ('62', 'UNDELIVERED', 'SS7'),
    ('71', 'UNDELIVERED', 'SS7'),
    ('72', 'UNDELIVERED', 'SS7'),
    ('300', 'UNDELIVERED', 'SS7'),
    ('507', 'UNDELIVERED', 'SS7'),
    ('509', 'UNDELIVERED', 'SS7'),
    ('510', 'UNDELIVERED', 'SS7'),
    ('511', 'UNDELIVERED', 'SS7'),

    -- REJECTED HTTP
    ('401', 'REJECTED', 'HTTP'),
    ('403', 'REJECTED', 'HTTP'),
    ('405', 'REJECTED', 'HTTP'),
    ('409', 'REJECTED', 'HTTP'),
    ('415', 'REJECTED', 'HTTP'),
    ('500', 'REJECTED', 'HTTP'),

    -- UNDELIVERED HTTP
    ('400', 'UNDELIVERED', 'HTTP'),
    ('404', 'UNDELIVERED', 'HTTP'),
    ('408', 'UNDELIVERED', 'HTTP'),
    ('410', 'UNDELIVERED', 'HTTP'),
    ('429', 'UNDELIVERED', 'HTTP'),
    ('501', 'UNDELIVERED', 'HTTP'),
    ('502', 'UNDELIVERED', 'HTTP'),
    ('503', 'UNDELIVERED', 'HTTP'),
    ('504', 'UNDELIVERED', 'HTTP');

CREATE TABLE public.sip_gateways
(
    network_id integer NOT NULL,

    created_at timestamp(6) without time zone,
    created_by_id integer,
    updated_at timestamp(6) without time zone,
    updated_by_id integer,

    enabled integer DEFAULT 1,

    name text NOT NULL,
    external_id            character varying(255),
    status                 text    DEFAULT 'STARTED'::text,
    protocol               text    DEFAULT 'SIP'::text,
    ip_address text NOT NULL,
    port integer NOT NULL,
    transport text NOT NULL,
    transaction_timeout integer NOT NULL DEFAULT 32000,
    retransmission_base_interval_ms integer NOT NULL DEFAULT 500,
    retransmission_max_interval_ms integer NOT NULL DEFAULT 4000,
    network_timeout_ms integer NOT NULL DEFAULT 5000,

    thread_pool_size integer NOT NULL DEFAULT 8,
    retransmission_filter boolean DEFAULT true NOT NULL,
    max_message_size integer NOT NULL DEFAULT 1048576,

    routing_enable_ss7 boolean DEFAULT false NOT NULL,
    routing_enable_diameter boolean DEFAULT false NOT NULL,

    routing_registration_traffic_ss7_gateway_id integer,
    routing_registration_traffic_diameter_gateway_id integer,
    routing_ussi_traffic_ss7_gateway_id integer,

    register_max_expires integer,
    ipsmgw_user text,
    ipsmgw_domain text,
    ims_domain text,
    ims_ccf text,
    ims_ecf text,

    subscribe_target_host text,
    subscribe_target_port integer,
    subscribe_target_transport text,

    local_via_host text,
    auto_retry_error_code text DEFAULT ''::text,
    no_retry_error_code character varying(255),
    retry_alternate_destination_error_code character varying(255),
    ussi_default_datacoding text,

    CONSTRAINT sip_settings_pkey
    PRIMARY KEY (network_id),

    CONSTRAINT sip_gateways_network_id_fk
    FOREIGN KEY (network_id)
    REFERENCES public.sequence_networks_id(id),

    CONSTRAINT sip_gateways_created_by_fk
    FOREIGN KEY (created_by_id)
    REFERENCES public.users(id),

    CONSTRAINT sip_gateways_updated_by_fk
    FOREIGN KEY (updated_by_id)
    REFERENCES public.users(id),

    CONSTRAINT sip_gateways_name_uk
    UNIQUE (name)
);
