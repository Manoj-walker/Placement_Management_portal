package edu.nit.placement.model.notification;

/**
 * Delivery context used by Notification.send(). Implemented by the service
 * layer so domain objects stay free of framework dependencies.
 */
public interface NotificationContext {

    /** Persist an in-app notification. */
    void persist(InAppNotification notification);

    /** Send an email notification (failures are logged, not thrown). */
    void email(EmailNotification notification);
}
