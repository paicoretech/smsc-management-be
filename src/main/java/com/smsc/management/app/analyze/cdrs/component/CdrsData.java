package com.smsc.management.app.analyze.cdrs.component;

import com.smsc.management.app.analyze.cdrs.dto.CdrsFilterDataDTO;
import com.smsc.management.app.analyze.utils.Utils;
import com.smsc.management.app.report.utils.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.smsc.management.app.analyze.utils.HelperQuery.COUNT_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.COUNT_QUERY_BY_REPORT_TABLE;
import static com.smsc.management.app.analyze.utils.HelperQuery.DATA_REPORT_CDRS_DETAILED_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.DATA_REPORT_CDRS_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.DROP_REPORT_TABLE;
import static com.smsc.management.app.analyze.utils.HelperQuery.NETWORK_QUERY;
import static com.smsc.management.app.analyze.utils.HelperQuery.PAGINATION_QUERY_REPORT_CDR;
import static com.smsc.management.app.analyze.utils.HelperQuery.PAGINATION_QUERY_REPORT_DETAILED;
import static com.smsc.management.utils.Constants.TABLE_ALIAS_CDR;
import static com.smsc.management.utils.Constants.TABLE_CDR_NAME;


@Slf4j
@Component
@RequiredArgsConstructor
public class CdrsData {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Utils utils;

    public CdrsFilterDataDTO filterData(Map<String, Object> filterParameters) {
        // base query
        String baseQuery = String.format("FROM %s %s", TABLE_CDR_NAME, TABLE_ALIAS_CDR);
        String whereClause = utils.prepareFiltersForWhereClause(filterParameters).toString();

        // Pagination query
        int currentPage = (int) filterParameters.getOrDefault("offset", 1);
        int pageSize = (int) filterParameters.getOrDefault("limit", 10);
        String paginationQuery = utils.createPaginationQuery(pageSize, currentPage).toString();
        // order query
        String orderQuery = " ORDER BY record_date DESC ";

        // Paginated data
        long totalElements = this.getTotalElementsWithFilterParameter(whereClause, filterParameters);
        String fullQuery = String.format("SELECT * %s %s %s %s", baseQuery, whereClause, orderQuery, paginationQuery);
        return this.executeQuery(fullQuery, totalElements, filterParameters, currentPage, pageSize);
    }

    public long getTotalElementsWithFilterParameter(String whereClause, Map<String, Object> filterParameters) {
        long totalElements = 0;
        String countQuery = String.format(COUNT_QUERY, whereClause);
        log.debug("countQueryMock: {}", countQuery);
        Long countResponse = jdbcTemplate.queryForObject(countQuery, filterParameters, Long.class);
        if (Objects.nonNull(countResponse)) {
            totalElements = countResponse;
        }
        return totalElements;
    }

    public boolean createReportDataTable(Map<String, Object> filterParameters, FileType fileType, String tableName) {
        try {
            String fullQuery;
            String whereClause = utils.prepareFiltersForWhereClause(filterParameters).toString();

            if (FileType.CDRS.equals(fileType)) {
                fullQuery = String.format(DATA_REPORT_CDRS_QUERY, tableName, NETWORK_QUERY, whereClause);
            } else {
                fullQuery = String.format(DATA_REPORT_CDRS_DETAILED_QUERY, tableName, NETWORK_QUERY, whereClause);
            }

            log.debug("fullQuery for temp table: {}", fullQuery);
            jdbcTemplate.update(fullQuery, filterParameters);
            return true;
        } catch (Exception e) {
            log.error("Error while creating CDRs data table", e);
        }
        return false;
    }

    public List<Map<String, Object>> getSimpleDataForHeaders(String tableName, FileType fileType) {
        String fullQuery = String.format(PAGINATION_QUERY_REPORT_CDR, tableName);
        if (fileType.equals(FileType.CDRS_DETAILED_REPORT)) {
            fullQuery = String.format(PAGINATION_QUERY_REPORT_DETAILED, tableName);
        }
        fullQuery = fullQuery + " LIMIT 1";

        return jdbcTemplate.queryForList(fullQuery, new HashMap<>());
    }

    public String getQueryForData(String tableName, FileType fileType) {
        String fullQuery = String.format(PAGINATION_QUERY_REPORT_CDR, tableName);
        if (fileType.equals(FileType.CDRS_DETAILED_REPORT)) {
            fullQuery = String.format(PAGINATION_QUERY_REPORT_DETAILED, tableName);
        }

        return fullQuery;
    }

    public long getTotalElements(String tableName) {
        long totalElements = 0;
        String countQuery = String.format(COUNT_QUERY_BY_REPORT_TABLE, tableName);
        log.debug("countQuery: {}", countQuery);
        Long countResponse = jdbcTemplate.queryForObject(countQuery, new LinkedHashMap<>(), Long.class);
        if (Objects.nonNull(countResponse)) {
            totalElements = countResponse;
        }
        return totalElements;
    }

    private CdrsFilterDataDTO executeQuery(String fullQuery, long totalCount, Map<String, Object> filterParameters, int currentPage, int pageSize) {
        log.debug("Pagination fullQuery: {}", fullQuery);
        List<Map<String, Object>> data = jdbcTemplate.queryForList(fullQuery, filterParameters);
        CdrsFilterDataDTO dataDTO = new CdrsFilterDataDTO();
        dataDTO.setData(data);
        this.completePaginationData(dataDTO, totalCount, pageSize, currentPage);
        return dataDTO;
    }

    private void completePaginationData(CdrsFilterDataDTO dataDTO, long totalElements, int pageSize, int currentPage) {

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLastPage = (currentPage == totalPages || totalPages == 0);
        boolean isFirstPage = (currentPage == 1);
        boolean hasPreviousPage = (currentPage > 1);
        boolean hasNextPage = (currentPage < totalPages);

        dataDTO.setPage(currentPage);
        dataDTO.setPageSize(pageSize);
        dataDTO.setTotalElements(totalElements);
        dataDTO.setTotalPages(totalPages);
        dataDTO.setFirstPage(isFirstPage);
        dataDTO.setLastPage(isLastPage);
        dataDTO.setHasPrevious(hasPreviousPage);
        dataDTO.setHasNext(hasNextPage);
    }

    public void deleteReportDataTable(String tableName) {
        String query = String.format(DROP_REPORT_TABLE, tableName);
        jdbcTemplate.getJdbcTemplate().execute(query);
    }
}
