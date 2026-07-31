package com.smsc.management.app.broadcast.utils;

import com.smsc.management.exception.SmscBackendException;

public class Utils {
    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static void validateBroadcastStatusAllowed(BroadcastStatus currentStatus, BroadcastStatus newStatus) {
        String status = currentStatus.name() + "_TO_" + newStatus.name();
        boolean allowed = switch (status) {
            case "DRAFT_TO_UPDATING",
                 "DRAFT_TO_DELETED",
                 "REJECTED_TO_UPDATING",
                 "REJECTED_TO_DELETED",
                 "CANCELED_TO_UPDATING",
                 "FAILED_TO_UPDATING",
                 "FAILED_TO_DELETED",
                 "PENDING_TO_APPROVED",
                 "PENDING_TO_REJECTED",
                 "SCHEDULED_TO_PROCESSING",
                 "SCHEDULED_TO_CANCELED",
                 "COMPLETED_TO_DELETED",
                 "CREATING_TO_CANCELED",
                 "PROCESSING_TO_CANCELED"-> true;
            default -> false;
        };

        if (!allowed) {
            throw new SmscBackendException("New broadcast status is not allowed: " + status);
        }
    }
}
