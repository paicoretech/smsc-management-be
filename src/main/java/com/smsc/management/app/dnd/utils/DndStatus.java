package com.smsc.management.app.dnd.utils;

public enum DndStatus {
    CREATING,
    ACTIVATING,
    ACTIVE,
    DISABLED,
    FAILED;

    public String getName() {
        return this.name();
    }
}