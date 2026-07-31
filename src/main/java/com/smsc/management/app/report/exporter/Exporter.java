package com.smsc.management.app.report.exporter;

import com.smsc.management.app.analyze.cdrs.component.CdrsData;
import com.smsc.management.app.analyze.reports.component.ReportData;
import com.smsc.management.app.report.model.entity.ReportFile;
import com.smsc.management.app.report.utils.FileType;

import java.util.Map;

public interface Exporter {

    void exportCdr(Map<String, Object> filters, CdrsData cdrsData, ReportFile reportFile, FileType fileType);

    void exportCdrSummary(Map<String, Object> filters, ReportData reportData, ReportFile reportFile, FileType fileType);
}
