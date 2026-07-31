package com.smsc.management.app.analyze.dashboards.component;

import com.smsc.management.app.analyze.dashboards.dto.DashboardsDTO;
import com.smsc.management.app.analyze.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.smsc.management.utils.Constants.TABLE_ALIAS_CDR;
import static com.smsc.management.utils.Constants.TABLE_CDR_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardsData {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Utils utils;

    public DashboardsDTO filterDataForDashboard(Map<String, Object> filterParameters) {
        String baseQuery = String.format("FROM %s %s", TABLE_CDR_NAME, TABLE_ALIAS_CDR);
        String whereClause = utils.prepareFiltersForWhereClause(filterParameters).toString();

        String fullQuery = String.format("""
                SELECT
                    TO_CHAR(c.record_date, 'YYYY-MM-DD') AS date,
                    COUNT(*) FILTER (WHERE c.status = 'SUCCESS') AS sms_delivery,
                    COUNT(*) FILTER (WHERE c.status = 'FAILED') AS sms_failed,
                    COUNT(*) AS total
                %s
                %s
                GROUP BY TO_CHAR(c.record_date, 'YYYY-MM-DD')
                ORDER BY TO_CHAR(c.record_date, 'YYYY-MM-DD') ASC;
                """, baseQuery, whereClause);

        String fullQueryTotal = String.format("""
                SELECT
                    COUNT(*) FILTER (WHERE c.status = 'SUCCESS') AS sms_delivery,
                    COUNT(*) FILTER (WHERE c.status = 'FAILED') AS sms_failed,
                    COUNT(*) AS total
                %s %s
                """, baseQuery, whereClause);

        List<Map<String, Object>> data = jdbcTemplate.queryForList(fullQuery, filterParameters);
        Map<String, Object> counts = jdbcTemplate.queryForMap(fullQueryTotal, filterParameters);

        DashboardsDTO dashboardsDTO = new DashboardsDTO();
        dashboardsDTO.setData(data);
        this.completeTotalMetrics(dashboardsDTO, counts);

        return dashboardsDTO;
    }

    private void completeTotalMetrics(DashboardsDTO dashboardsDTO, Map<String, Object> counts) {
        // total data
        long totalRows = (Long) counts.getOrDefault("total", 0);
        long totalDeliveryRows = (Long) counts.getOrDefault("sms_delivery", 0);
        long totalFailedRows = (Long) counts.getOrDefault("sms_failed", 0);
        double smsDeliveryRate = totalRows == 0 ? 0 : (double) (totalDeliveryRows * 100) / totalRows;
        double smsFailedRate = totalRows == 0 ? 0 : (double) (totalFailedRows * 100) / totalRows;

        dashboardsDTO.setTotal(totalRows);
        dashboardsDTO.setSmsDelivery(totalDeliveryRows);
        dashboardsDTO.setSmsFailed(totalFailedRows);
        dashboardsDTO.setSmsDeliveryRate(smsDeliveryRate);
        dashboardsDTO.setSmsFailedRate(smsFailedRate);
    }
}
