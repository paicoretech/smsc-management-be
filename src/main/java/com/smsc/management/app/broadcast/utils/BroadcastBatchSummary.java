package com.smsc.management.app.broadcast.utils;

public record BroadcastBatchSummary(int pagesProcessed, long totalMessages, BroadcastBatchOutcome outcome) {}

