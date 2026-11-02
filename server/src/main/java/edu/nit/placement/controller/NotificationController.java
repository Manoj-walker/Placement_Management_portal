package edu.nit.placement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.nit.placement.dto.NotificationDtos.ReadRequest;
import edu.nit.placement.model.notification.InAppNotification;
import edu.nit.placement.model.user.User;
import edu.nit.placement.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<InAppNotification> inbox(@AuthenticationPrincipal User principal) {
        return notificationService.inbox(principal.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(@AuthenticationPrincipal User principal) {
        return Map.of("count", notificationService.unreadCount(principal.getId()));
    }

    @PatchMapping("/read-all")
    public Map<String, String> markAllRead(@AuthenticationPrincipal User principal) {
        notificationService.markAllRead(principal.getId());
        return Map.of("message", "All notifications marked as read");
    }

    @PatchMapping("/{id}/read")
    public Map<String, String> markRead(@AuthenticationPrincipal User principal,
            @PathVariable Long id, @RequestBody ReadRequest request) {
        notificationService.markRead(principal.getId(), id, request.read());
        return Map.of("message", "Notification updated");
    }
}
