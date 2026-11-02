package edu.nit.placement.model.notification;

/** Website notification — kept in the in-app notification inbox. */
public class InAppNotification extends Notification {

    public InAppNotification() {
    }

    public InAppNotification(Long userId, String message, String type) {
        setUserId(userId);
        setMessage(message);
        setType(type);
    }

    @Override
    public void send(NotificationContext context) {
        context.persist(this);
    }

    @Override
    public String channel() {
        return "IN_APP";
    }
}
