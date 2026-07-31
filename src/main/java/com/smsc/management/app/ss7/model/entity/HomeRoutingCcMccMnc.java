package com.smsc.management.app.ss7.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "home_routing_cc_mcc_mnc",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hr_cc_mccmnc_per_hr",
                        columnNames = {"ss7_home_routing_id", "country_code", "mcc_mnc"}
                )
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@SequenceGenerator(name = "home_routing_cc_mcc_mnc_id_seq", sequenceName = "home_routing_cc_mcc_mnc_id_seq", allocationSize = 1)
public class HomeRoutingCcMccMnc {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "home_routing_cc_mcc_mnc_id_seq")
    private long id;

    @Column(name = "country_code", columnDefinition = "text default '-1'", nullable = false)
    private String countryCode = "";

    @Column(name = "mcc_mnc", columnDefinition = "text", nullable = false)
    private String mccMnc;

    @Column(name = "smsc", columnDefinition = "text default ''")
    private String smsc = "";

    @Column(name = "ss7_home_routing_id", nullable = false)
    private int ss7HomeRoutingId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ss7_home_routing_id", insertable = false, updatable = false)
    private HomeRouting ss7HomeRouting;
}
