package com.smsc.management.app.broadcast.utils;

public enum BroadcastStatus {
    CREATING,
    CREATED,
    FAILED,
    PROCESSING,
    COMPLETED,
    SCHEDULED,
    DRAFT,
    UPDATING,
    APPROVED,
    REJECTED,
    PENDING,
    CANCELED,
    DELETED;

    public String getName() {
        return this.name();
    }

    public boolean isEqual(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BroadcastStatus that = (BroadcastStatus) o;
        return this.name().equalsIgnoreCase(that.name());
    }
}
