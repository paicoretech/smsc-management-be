package com.smsc.management.app.settings.model.entity;

import com.smsc.management.app.catalog.model.entity.EncodingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "General_settings_smppHttp")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class GeneralSettingsSmppHttp {
	@Id
	private int id = 1;
	
	@Column(name = "validity_period")
	public int validityPeriod = 60;
	
	@Column(name = "max_validity_period")
	private int maxValidityPeriod = 240;
	
	@Column(name = "source_addr_ton")
	private int sourceAddrTon = 1;
	
	@Column(name = "source_addr_npi")
	private int sourceAddrNpi = 2;
	
	@Column(name = "dest_addr_ton")
	private int destAddrTon = 1;
	
	@Column(name = "dest_addr_npi")
	private int destAddrNpi = 2;
	
	@Column(name = "encoding_iso88591")
	private int encodingIso88591 = 3;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="encoding_iso88591", insertable=false, updatable=false)
	private EncodingType encodingTypeIso;
	
	@Column(name = "encoding_gsm7")
	private int encodingGsm7 = 0;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="encoding_gsm7", insertable=false, updatable=false)
	private EncodingType encodingType;
	
	@Column(name = "encoding_ucs2")
	private int encodingUcs2 = 2;
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name="encoding_ucs2", insertable=false, updatable=false)
	private EncodingType encodingTypeUcs2;
}
