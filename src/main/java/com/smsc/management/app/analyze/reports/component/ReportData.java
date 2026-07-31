package com.smsc.management.app.analyze.reports.component;

import com.smsc.management.app.analyze.utils.Utils;
import com.smsc.management.app.report.utils.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.smsc.management.app.analyze.utils.HelperQuery.NETWORK_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.REPORT_ACCOUNT_DESTINATION_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.REPORT_ACCOUNT_DESTINATION_TRAFFIC_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.REPORT_ACCOUNT_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.REPORT_ACCOUNT_TRAFFIC_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.REPORT_BROADCAST_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.REPORT_USER_AND_SENDER_TRAFFIC_QUERY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportData {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Utils utils;

    public List<Map<String, Object>> createReportData(Map<String, Object> filterParameters, FileType fileType) {
        String fullQuery;
        String whereClause = utils.prepareFiltersForWhereClause(filterParameters).toString();

        switch (fileType) {
            case SUMMARY_CDR_ACCOUNT -> fullQuery = String.format(REPORT_ACCOUNT_QUERY, NETWORK_QUERY, whereClause);
            case SUMMARY_CDR_ACCOUNT_TRAFFIC -> fullQuery = String.format(REPORT_ACCOUNT_TRAFFIC_QUERY, NETWORK_QUERY, whereClause);
            case SUMMARY_CDR_BROADCAST -> fullQuery = String.format(REPORT_BROADCAST_QUERY, whereClause);
            case SUMMARY_CDR_DESTINATION -> fullQuery = String.format(REPORT_ACCOUNT_DESTINATION_QUERY, NETWORK_QUERY, whereClause);
            case SUMMARY_CDR_DESTINATION_TRAFFIC ->  fullQuery = String.format(REPORT_ACCOUNT_DESTINATION_TRAFFIC_QUERY, NETWORK_QUERY, whereClause);
            case SUMMARY_CDR_USER_AND_SENDER_TRAFFIC -> fullQuery = String.format(REPORT_USER_AND_SENDER_TRAFFIC_QUERY, whereClause);
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }

        log.info(fullQuery);
        return jdbcTemplate.queryForList(fullQuery, filterParameters);
    }

}
