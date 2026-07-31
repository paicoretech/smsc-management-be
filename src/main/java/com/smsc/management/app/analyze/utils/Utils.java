package com.smsc.management.app.analyze.utils;

import com.smsc.management.app.broadcast.model.repository.BroadcastRepository;
import com.smsc.management.utils.UtilsBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.smsc.management.utils.Constants.AND_CONDITION;
import static com.smsc.management.utils.Constants.BROADCAST_FILTER_USER;
import static com.smsc.management.utils.Constants.END_DATETIME_KEY;
import static com.smsc.management.utils.Constants.FILTERS_KEY_MAPPING;
import static com.smsc.management.utils.Constants.SEARCH_FILTER;
import static com.smsc.management.utils.Constants.START_DATETIME_KEY;
import static com.smsc.management.utils.Constants.TABLE_ALIAS_CDR;

@Slf4j
@Component
@RequiredArgsConstructor
public class Utils {
    private final BroadcastRepository broadcastRepository;
    private final UtilsBase utilsBase;

    public StringBuilder prepareFiltersForWhereClause(Map<String, Object> filterParameters) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        this.addDateTimeFilter(filterParameters, whereClause);

        List<String> excludeFields = List.of(START_DATETIME_KEY, END_DATETIME_KEY, BROADCAST_FILTER_USER);
        FILTERS_KEY_MAPPING.keySet().forEach(paramName -> {
            Object value = filterParameters.getOrDefault(paramName, null);
            if (Objects.nonNull(value) && !excludeFields.contains(paramName)) {
                String columnName = FILTERS_KEY_MAPPING.get(paramName);

                if (value instanceof Collection) {
                    whereClause.append(AND_CONDITION).append(columnName).append(" IN (:").append(paramName).append(")");
                } else {
                    whereClause.append(AND_CONDITION).append(columnName).append(" = :").append(paramName);
                }
            }
        });

        this.addBroadcastUserFilter(filterParameters, whereClause);

        Object searchFilterValue = filterParameters.getOrDefault(SEARCH_FILTER, null);
        if (Objects.nonNull(searchFilterValue)) {
            whereClause.append(AND_CONDITION)
                    .append("(")
                    .append(TABLE_ALIAS_CDR + ".message_id").append(" = :").append(SEARCH_FILTER)
                    .append(" OR ").append(TABLE_ALIAS_CDR + ".status_code").append(" = :").append(SEARCH_FILTER)
                    .append(" OR ").append(TABLE_ALIAS_CDR + ".addr_src_digits").append(" = :").append(SEARCH_FILTER)
                    .append(" OR ").append(TABLE_ALIAS_CDR + ".addr_dst_digits").append(" = :").append(SEARCH_FILTER)
                    .append(")");
        }

        return whereClause;
    }

    public StringBuilder createPaginationQuery(int limit, int offset) {
        return new StringBuilder().append(" LIMIT ")
                .append(limit)
                .append(" OFFSET ")
                .append((offset - 1) * limit);
    }

    private void addDateTimeFilter(Map<String, Object> filterParameters, StringBuilder whereClause) {
        Object value = filterParameters.getOrDefault(START_DATETIME_KEY, null);
        if (Objects.nonNull(value)) {
            String columnName = FILTERS_KEY_MAPPING.get(START_DATETIME_KEY);
            whereClause.append(AND_CONDITION).append(columnName).append(" >= to_timestamp(:").append(START_DATETIME_KEY).append(", 'YYYY-MM-DD HH24:MI:SS')");
            filterParameters.put(START_DATETIME_KEY, utilsBase.convertStringUTCToLocalTimeZone(value.toString(), "yyyy-MM-dd HH:mm:ss"));
        }

        value = filterParameters.getOrDefault(END_DATETIME_KEY, null);
        if (Objects.nonNull(value)) {
            String columnName = FILTERS_KEY_MAPPING.get(END_DATETIME_KEY);
            whereClause.append(AND_CONDITION).append(columnName).append(" <= to_timestamp(:").append(END_DATETIME_KEY).append(", 'YYYY-MM-DD HH24:MI:SS')");
            filterParameters.put(END_DATETIME_KEY, utilsBase.convertStringUTCToLocalTimeZone(value.toString(), "yyyy-MM-dd HH:mm:ss"));
        }
    }

    @SuppressWarnings("unchecked")
    private void addBroadcastUserFilter(Map<String, Object> filterParameters, StringBuilder whereClause) {
        Object value = filterParameters.getOrDefault(BROADCAST_FILTER_USER, null);
        if (Objects.nonNull(value)) {
            List<Integer> broadcastIds;
            if (value instanceof Collection) {
                broadcastIds = broadcastRepository.findIdsByCreatedByUserIds((List<Integer>) value);
            } else {
                broadcastIds = broadcastRepository.findIdsByCreatedByUserId((int) value);
            }

            if (!broadcastIds.isEmpty()) {
                String columnName = FILTERS_KEY_MAPPING.get(BROADCAST_FILTER_USER);
                whereClause.append(AND_CONDITION).append(columnName).append(" IN (:").append(BROADCAST_FILTER_USER).append(")");

                // reset value to add in the query execution
                filterParameters.put(BROADCAST_FILTER_USER, broadcastIds);
            }
        }
    }

}
