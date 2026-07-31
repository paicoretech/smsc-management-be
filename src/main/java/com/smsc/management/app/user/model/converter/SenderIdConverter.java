package com.smsc.management.app.user.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class SenderIdConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        attribute.stream()
                .filter(id -> id != null && id.trim().length() > 11)
                .findFirst()
                .ifPresent(id -> {
                    throw new IllegalArgumentException(
                            "Sender ID '" + id.trim() + "' exceeds maximum length of 11 characters"
                    );
                });
        return String.join(",", attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
