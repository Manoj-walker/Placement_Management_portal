package edu.nit.placement.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import edu.nit.placement.dao.EmailLogDAO;
import edu.nit.placement.dao.NotificationDAO;
import edu.nit.placement.model.notification.EmailNotification;
import edu.nit.placement.model.notification.InAppNotification;
import edu.nit.placement.model.notification.Notification;
import edu.nit.placement.model.notification.NotificationContext;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;

/**
 * Owns notification delivery. Implements NotificationContext so that
 * Notification.send() can dispatch polymorphically to the in-app inbox or SMTP.
 */
@Service
public class NotificationService implements NotificationContext {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDAO notificationDAO;
    private final EmailLogDAO emailLogDAO;
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public NotificationService(NotificationDAO notificationDAO,
            EmailLogDAO emailLogDAO,
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.mail.from:no-reply@placement.nit.edu}") String fromAddress) {
        this.notificationDAO = notificationDAO;
        this.emailLogDAO = emailLogDAO;
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    /** In-app + (optionally) email notification for one user. */
    public void notifyUser(User user, String message, String type) {
        if (user == null || user.getId() == null) {
            return;
        }
        Notification inApp = new InAppNotification(user.getId(), message, type);
        inApp.send(this);
        if (mailEnabled && user.getEmail() != null) {
            Notification email = new EmailNotification(user.getId(), user.getEmail(),
                    subjectFor(type), message, type);
            email.send(this);
        }
    }

    /** Same as notifyUser but skips messages already present in the inbox. */
    public void notifyUserOnce(User user, String message, String type) {
        if (user == null || user.getId() == null) {
            return;
        }
        if (notificationDAO.existsForUser(user.getId(), message)) {
            return;
        }
        notifyUser(user, message, type);
    }

    public void notifyStudents(List<Student> students, String message, String type) {
        for (Student student : students) {
            notifyUserOnce(student, message, type);
        }
    }

    @Override
    public void persist(InAppNotification notification) {
        notificationDAO.insert(notification);
    }

    @Override
    public void email(EmailNotification notification) {
        Long rowId = notificationDAO.insertEmailAttempt(notification.getUserId(),
                notification.getEmailAddress(), notification.getSubject(),
                notification.getType(), notification.getMessage());
        if (!mailEnabled) {
            emailLogDAO.finish(rowId, "SKIPPED", "Email delivery disabled (app.mail.enabled=false)");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(notification.getEmailAddress());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());
            mailSender.send(message);
            emailLogDAO.finish(rowId, "SENT", null);
        } catch (Exception ex) {
            log.warn("Email to {} failed: {}", notification.getEmailAddress(), ex.getMessage());
            emailLogDAO.finish(rowId, "FAILED", ex.getMessage());
        }
    }

    public List<InAppNotification> inbox(Long userId) {
        return notificationDAO.listByUser(userId);
    }

    public long unreadCount(Long userId) {
        return notificationDAO.unreadCount(userId);
    }

    public void markAllRead(Long userId) {
        notificationDAO.markAllRead(userId);
    }

    public void markRead(Long userId, Long id, boolean read) {
        notificationDAO.setRead(userId, id, read);
    }

    private String subjectFor(String type) {
        return switch (type == null ? "" : type) {
            case "REMINDER" -> "Placement NIT — deadline reminder";
            case "RESULT" -> "Placement NIT — result published";
            case "SCHEDULE" -> "Placement NIT — schedule update";
            default -> "Placement NIT — new drive published";
        };
    }
}
