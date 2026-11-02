package edu.nit.placement.dto;

/** Notification actions. */
public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record ReadRequest(boolean read) {
    }
}
