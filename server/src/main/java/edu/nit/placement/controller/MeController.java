package edu.nit.placement.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.nit.placement.dto.StudentDtos.PasswordChangeRequest;
import edu.nit.placement.dto.StudentDtos.ProfileUpdateRequest;
import edu.nit.placement.model.user.User;
import edu.nit.placement.service.AuthService;
import edu.nit.placement.service.NotificationService;

/** The signed-in user's own profile and password. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AuthService authService;
    private final NotificationService notificationService;

    public MeController(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public User me(@AuthenticationPrincipal User principal) {
        return authService.currentUser(principal);
    }

    @PutMapping("/profile")
    public User updateProfile(@AuthenticationPrincipal User principal,
            @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(principal, request);
    }

    @PostMapping("/password")
    public Map<String, String> changePassword(@AuthenticationPrincipal User principal,
            @RequestBody PasswordChangeRequest request) {
        authService.changePassword(principal, request);
        return Map.of("message", "Password updated");
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(@AuthenticationPrincipal User principal) {
        return Map.of("count", notificationService.unreadCount(principal.getId()));
    }
}
