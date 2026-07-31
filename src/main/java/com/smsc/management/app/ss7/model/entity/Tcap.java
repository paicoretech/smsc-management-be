package com.smsc.management.app.ss7.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tcap")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "tcap_id_seq", sequenceName = "tcap_id_seq", allocationSize = 1)
public class Tcap {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tcap_id_seq")
    private int id;

    @Column(name = "network_id", unique = true)
    private int networkId;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", insertable = false, updatable = false)
    private Ss7Gateways ss7Gateways;

    @Column(name = "ssn_list")
    private String ssnList;

    @Column(name = "preview_mode")
    private boolean previewMode;

    @Column(name = "dialog_idle_timeout")
    private int dialogIdleTimeout;

    @Column(name = "invoke_timeout")
    private int invokeTimeout;

    @Column(name = "dialog_id_range_start")
    private int dialogIdRangeStart;

    @Column(name = "dialog_id_range_end")
    private int dialogIdRangeEnd;

    @Column(name = "max_dialogs")
    private int maxDialogs;

    @Column(name = "do_not_send_protocol_version")
    private boolean doNotSendProtocolVersion;

    @Column(name = "swap_tcap_id_enabled")
    private boolean swapTcapIdEnabled;

    @Column(name = "sls_range_id")
    private String slsRangeId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="sls_range_id", insertable=false, updatable=false)
    private SlsRange slsRange;

    @Column(name = "exr_delay_thr1", columnDefinition = "numeric")
    private float exrDelayThr1;

    @Column(name = "exr_delay_thr2", columnDefinition = "numeric")
    private float exrDelayThr2;

    @Column(name = "exr_delay_thr3", columnDefinition = "numeric")
    private float exrDelayThr3;

    @Column(name = "exr_back_to_normal_delay_thr1", columnDefinition = "numeric")
    private float exrBackToNormalDelayThr1;

    @Column(name = "exr_back_to_normal_delay_thr2", columnDefinition = "numeric")
    private float exrBackToNormalDelayThr2;

    @Column(name = "exr_back_to_normal_delay_thr3", columnDefinition = "numeric")
    private float exrBackToNormalDelayThr3;

    @Column(name = "memory_monitor_thr1", columnDefinition = "numeric")
    private float memoryMonitorThr1;

    @Column(name = "memory_monitor_thr2", columnDefinition = "numeric")
    private float memoryMonitorThr2;

    @Column(name = "memory_monitor_thr3", columnDefinition = "numeric")
    private float memoryMonitorThr3;

    @Column(name = "mem_back_to_normal_delay_thr1", columnDefinition = "numeric")
    private float memBackToNormalDelayThr1;

    @Column(name = "mem_back_to_normal_delay_thr2", columnDefinition = "numeric")
    private float memBackToNormalDelayThr2;

    @Column(name = "mem_back_to_normal_delay_thr3", columnDefinition = "numeric")
    private float memBackToNormalDelayThr3;

    @Column(name = "blocking_incoming_tcap_messages")
    private boolean blockingIncomingTcapMessages;
}
