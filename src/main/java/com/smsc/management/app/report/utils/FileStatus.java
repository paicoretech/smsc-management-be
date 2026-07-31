package com.smsc.management.app.report.utils;


public enum FileStatus {
    CREATING,
    COMPLETED,
    FAILED,
    TOKEN_EXPIRED;

    public String getName() {
        return this.name();
    }

    public boolean isEqual(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileStatus that = (FileStatus) o;
        return this.name().equalsIgnoreCase(that.name());
    }
}
