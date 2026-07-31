package com.smsc.management.app.broadcast.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceNumber {
    private SequenceNumber() {
        throw new IllegalStateException("SequenceNumber Class");
    }

    private static final AtomicInteger sequence = new AtomicInteger(0);

    public static Integer getNextSequence() {
        return sequence.incrementAndGet();
    }
}
