package com.smsc.management.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class CustomStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String parsedValue = parser.getText().trim();
        return parsedValue.isEmpty() ? null : parsedValue;
    }
}
