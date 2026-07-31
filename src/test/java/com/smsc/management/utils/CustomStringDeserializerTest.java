package com.smsc.management.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomStringDeserializerTest {

    private CustomStringDeserializer customStringDeserializer;
    private DeserializationContext deserializationContextMock;
    private JsonParser jsonParserMock;
    private static final String EXTERNAL_ID_TEST = "EXT-ID-0001";

    @BeforeEach
    public void setUp() {
        customStringDeserializer = new CustomStringDeserializer();
        jsonParserMock = mock(JsonParser.class);
        deserializationContextMock = mock(DeserializationContext.class);
    }

    @Test
    void deserializeEmptyStringTest() throws IOException {
        when(jsonParserMock.getText()).thenReturn(" ");
        String result = customStringDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertNull(result);
    }

    @Test
    void deserializeNonEmptyStringTest() throws IOException {
        when(jsonParserMock.getText()).thenReturn(EXTERNAL_ID_TEST);
        String result = customStringDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertEquals(EXTERNAL_ID_TEST, result);
    }

    @Test
    void deserializeWithSpacesStringTest() throws IOException {
        String trimmedExternalId = "   " + EXTERNAL_ID_TEST + "   ";
        when(jsonParserMock.getText()).thenReturn(trimmedExternalId);
        String result = customStringDeserializer.deserialize(jsonParserMock, deserializationContextMock);
        assertEquals(EXTERNAL_ID_TEST, result);
    }
}
