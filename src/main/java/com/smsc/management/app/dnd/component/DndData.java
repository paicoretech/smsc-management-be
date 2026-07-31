package com.smsc.management.app.dnd.component;

import com.smsc.management.app.dnd.dto.DndEntryMsisdnFilterDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DndData {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DndEntryMsisdnFilterDataDTO filterDndEntries(Map<String, Object> filterParameters) {
        // Extract parent_id from filters
        Integer parentId = (Integer) filterParameters.get("parent_id");
        if (Objects.isNull(parentId)) {
            throw new IllegalArgumentException("parent_id is required for DND entries filtering");
        }

        // Base query with search functionality
        String baseQuery = "FROM dnd_entry_msidn WHERE parent_id = :parent_id";
        String searchCondition = "";
        
        // Add search condition if search parameter is provided
        String search = (String) filterParameters.get("search");
        if (search != null && !search.trim().isEmpty()) {
            searchCondition = " AND msisdn ILIKE :search";
            filterParameters.put("search", "%" + search + "%");
        }
        
        baseQuery += searchCondition;

        // Pagination parameters
        int currentPage = (int) filterParameters.getOrDefault("offset", 1);
        int pageSize = (int) filterParameters.getOrDefault("limit", 10);

        // Create pagination query
        String paginationQuery = this.createPaginationQuery(pageSize, currentPage);

        // Get total elements with search consideration
        long totalElements = this.getTotalElements(parentId, search);

        // Full query for data
        String fullQuery = String.format("SELECT id, msisdn, parent_id %s %s",
                baseQuery, paginationQuery);

        return this.executeQuery(fullQuery, totalElements, filterParameters, currentPage, pageSize);
    }

    private long getTotalElements(Integer parentId, String search) {
        String countQuery = "SELECT COUNT(*) FROM dnd_entry_msidn WHERE parent_id = :parent_id";
        Map<String, Object> params = Map.of("parent_id", parentId);
        
        // Add search condition if provided
        if (search != null && !search.trim().isEmpty()) {
            countQuery += " AND msisdn ILIKE :search";
            params = Map.of("parent_id", parentId, "search", "%" + search + "%");
        }

        log.debug("countQuery: {}", countQuery);
        Long countResponse = jdbcTemplate.queryForObject(countQuery, params, Long.class);
        return Objects.nonNull(countResponse) ? countResponse : 0L;
    }

    private String createPaginationQuery(int limit, int offset) {
        return String.format(" LIMIT %d OFFSET %d", limit, (offset - 1) * limit);
    }

    private DndEntryMsisdnFilterDataDTO executeQuery(String fullQuery, long totalCount,
                                                     Map<String, Object> filterParameters, int currentPage, int pageSize) {
        log.debug("fullQuery: {}", fullQuery);
        List<Map<String, Object>> data = jdbcTemplate.queryForList(fullQuery, filterParameters);

        DndEntryMsisdnFilterDataDTO dataDTO = new DndEntryMsisdnFilterDataDTO();
        dataDTO.setData(data);
        this.completePaginationData(dataDTO, totalCount, pageSize, currentPage);
        return dataDTO;
    }

    private void completePaginationData(DndEntryMsisdnFilterDataDTO dataDTO, long totalElements,
                                        int pageSize, int currentPage) {
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
}