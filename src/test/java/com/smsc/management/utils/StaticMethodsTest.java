package com.smsc.management.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class StaticMethodsTest {
    @Test
    void fromJsonSuccess() {
        GlobalRecords.SystemIdInputParameter systemIdInputParameter = StaticMethods.fromJson("{\"network_id\":\"1\"}", GlobalRecords.SystemIdInputParameter.class);
        assertNotNull(systemIdInputParameter);
    }

    @Test
    void fromJsonFailure() {
        GlobalRecords.SystemIdInputParameter systemIdInputParameter = StaticMethods.fromJson("{\"system_id\":\"system_id\"", GlobalRecords.SystemIdInputParameter.class);
        assertNull(systemIdInputParameter);
    }

    @Test
    void toJsonSuccess() {
        String json = StaticMethods.toJson(new GlobalRecords.SystemIdInputParameter("123"));
        assertNotNull(json);
    }

    @Test
    void toJsonFailure() {
        String json = StaticMethods.toJson(new Object());
        assertNull(json);
    }

    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<StaticMethods> constructor = StaticMethods.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void testConverterStringToObjectWithDifferentTypeValue() {
        Object stringTest = StaticMethods.stringToObject("this a test");
        assertNotNull(stringTest);

        Object booleanTest = StaticMethods.stringToObject("true");
        assertInstanceOf(Boolean.class, booleanTest);

        Object intTest = StaticMethods.stringToObject("1234");
        assertInstanceOf(Integer.class, intTest);

        Object longTest = StaticMethods.stringToObject("15567822345667");
        assertInstanceOf(Long.class, longTest);

        Object doubleTest = StaticMethods.stringToObject("14.5");
        assertInstanceOf(Double.class, doubleTest);

        Object arrayIntTest = StaticMethods.stringToObject("[1,2,3,\"4\",\"test\"]");
        assertInstanceOf(ArrayList.class, arrayIntTest);

        Object arrayStringTest = StaticMethods.stringToObject("[\"1\",\"2\",\"3\",\"4\"]");
        assertInstanceOf(ArrayList.class, arrayStringTest);

        Object jsonTest = StaticMethods.stringToObject("[{\"p\":\"test\", \"v\": 1},{\"p\":\"otro\", \"v\":true}, {\"p\":\"otro2\", \"v\": [\"ere\",\"ertt\"]}]");
        assertInstanceOf(ArrayList.class, jsonTest);
    }

    @Test
    void applyForUpdateTest() {
        String stOk = "test";
        String stNull = null;
        String stEmpty = "";

        boolean resultOk = StaticMethods.applyForUpdate(stOk);
        boolean resultNull = StaticMethods.applyForUpdate(stNull);
        boolean resultEmpty = StaticMethods.applyForUpdate(stEmpty);

        assertTrue(resultOk);
        assertFalse(resultNull);
        assertFalse(resultEmpty);

        Integer intOk = 1;
        Integer intNull = null;

        boolean resultIntOk = StaticMethods.applyForUpdate(intOk);
        boolean resultIntNull = StaticMethods.applyForUpdate(intNull);

        assertTrue(resultIntOk);
        assertFalse(resultIntNull);

        List<String> stringListOk = List.of("test", "test2");
        List<String> stringListEmpty = new ArrayList<>();
        List<String> stringListNull = null;

        boolean resultStringListOk = StaticMethods.applyForUpdate(stringListOk);
        boolean resultStringListEmpty = StaticMethods.applyForUpdate(stringListEmpty);
        boolean resultStringListNull = StaticMethods.applyForUpdate(stringListNull);

        assertTrue(resultStringListOk);
        assertFalse(resultStringListEmpty);
        assertFalse(resultStringListNull);
    }
}