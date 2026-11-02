package edu.nit.placement.model.notification;

/** Email notification — sent through SMTP via the service layer. */
public class EmailNotification extends Notification {

    private String emailAddress;
    private String subject;

    public EmailNotification() {
    }

    public EmailNotification(Long userId, String emailAddress, String subject, String message) {
        setUserId(userId);
        setEmailAddress(emailAddress);
        setSubject(subject);
        setMessage(message);
        setType("EMAIL");
    }

    /** Alternative constructor keeping the original notification type for the audit row. */
    public EmailNotification(Long userId, String emailAddress, String subject,
            String message, String notificationType) {
        setUserId(userId);
        setEmailAddress(emailAddress);
        setSubject(subject);
        setMessage(message);
        setType(notificationType);
    }

    @Override
    public void send(NotificationContext context) {
        context.email(this);
    }

    @Override
    public String channel() {
        return "EMAIL";
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
