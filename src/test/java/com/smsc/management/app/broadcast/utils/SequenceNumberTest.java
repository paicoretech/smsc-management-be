package com.smsc.management.app.broadcast.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ExtendWith(MockitoExtension.class)
class SequenceNumberTest {
    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<SequenceNumber> constructor = SequenceNumber.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void getNextSequence() {
        Assertions.assertNotEquals(0, SequenceNumber.getNextSequence());
    }
}