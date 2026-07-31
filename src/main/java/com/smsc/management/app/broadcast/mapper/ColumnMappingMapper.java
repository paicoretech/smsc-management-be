package com.smsc.management.app.broadcast.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.utils.Converter;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class ColumnMappingMapper {

    public static String map(Map<String, String> columnMapping) {
        return Converter.valueAsString(columnMapping);
    }

    public static Map<String, String> map(String columnMappingJson) {
        if (columnMappingJson == null || columnMappingJson.trim().isEmpty()) {
            return null;
        }
        return Converter.stringToObject(columnMappingJson, new TypeReference<>() {});
    }
}
