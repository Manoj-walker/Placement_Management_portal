package edu.nit.placement.model.notification;

import java.time.LocalDateTime;

/**
 * Abstract notification. Concrete channels decide how send() works
 * (U5): InAppNotification stores it for the website, EmailNotification emails it.
 */
public abstract class Notification {

    private Long id;
    private Long userId;
    private String message;
    private String type = "ANNOUNCEMENT";
    private boolean read;
    private LocalDateTime createdAt;

    /** Polymorphic delivery. */
    public abstract void send(NotificationContext context);

    /** Channel name for logging/debugging. */
    public abstract String channel();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
