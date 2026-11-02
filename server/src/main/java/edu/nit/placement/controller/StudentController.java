package edu.nit.placement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.nit.placement.exception.ApiException;
import edu.nit.placement.model.announcement.Announcement;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;
import edu.nit.placement.service.AnnouncementService;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final AnnouncementService announcementService;

    public StudentController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    private Student student(User principal) {
        if (principal instanceof Student student) {
            return student;
        }
        throw ApiException.forbidden("Student account required");
    }

    @GetMapping("/dashboard")
    public List<Announcement> dashboard(@AuthenticationPrincipal User principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String filter) {
        return announcementService.dashboard(student(principal), search, filter);
    }

    @GetMapping("/companies")
    public List<Announcement> companies(@AuthenticationPrincipal User principal,
            @RequestParam(required = false) String search) {
        return announcementService.dashboard(student(principal), search, "all");
    }

    @GetMapping("/announcements/{id}")
    public Announcement detail(@AuthenticationPrincipal User principal, @PathVariable Long id) {
        return announcementService.detail(student(principal), id);
    }

    @PostMapping("/announcements/{id}/apply")
    public Announcement apply(@AuthenticationPrincipal User principal, @PathVariable Long id) {
        return announcementService.apply(student(principal), id);
    }

    @PostMapping("/announcements/{id}/bookmark")
    public Announcement bookmark(@AuthenticationPrincipal User principal, @PathVariable Long id) {
        return announcementService.bookmark(student(principal), id);
    }

    @DeleteMapping("/announcements/{id}/bookmark")
    public Map<String, String> removeBookmark(@AuthenticationPrincipal User principal, @PathVariable Long id) {
        announcementService.removeBookmark(student(principal), id);
        return Map.of("message", "Bookmark removed");
    }

    @GetMapping("/applications")
    public List<Announcement> applications(@AuthenticationPrincipal User principal) {
        return announcementService.applications(student(principal));
    }

    @GetMapping("/bookmarks")
    public List<Announcement> bookmarks(@AuthenticationPrincipal User principal) {
        return announcementService.dashboard(student(principal), null, "bookmarked");
    }

    @GetMapping("/deadlines")
    public List<Map<String, Object>> deadlines(@AuthenticationPrincipal User principal) {
        return announcementService.deadlines(student(principal));
    }

    @GetMapping("/results")
    public List<Map<String, Object>> results(@AuthenticationPrincipal User principal) {
        return announcementService.results(student(principal));
    }
}
