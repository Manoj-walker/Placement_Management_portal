package edu.nit.placement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.nit.placement.dto.AnnouncementDtos.AnnouncementRequest;
import edu.nit.placement.dto.AnnouncementDtos.ArchiveRequest;
import edu.nit.placement.dto.AnnouncementDtos.ResultRequest;
import edu.nit.placement.dto.AnnouncementDtos.ScheduleRequest;
import edu.nit.placement.service.AnnouncementService.JdDownload;
import edu.nit.placement.exception.ApiException;
import edu.nit.placement.model.announcement.Announcement;
import edu.nit.placement.model.user.User;
import edu.nit.placement.service.AnnouncementService;
import jakarta.validation.Valid;

/** Coordinator announcement management + results + schedules. */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public List<Announcement> list(@RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean archived) {
        return announcementService.list(search, archived);
    }

    @PostMapping
    public Announcement create(@AuthenticationPrincipal User principal,
            @Valid @RequestBody AnnouncementRequest request) {
        return announcementService.create(request, principal);
    }

    @GetMapping("/{id}")
    public Announcement get(@PathVariable Long id) {
        return announcementService.getOrThrow(id);
    }

    @PutMapping("/{id}")
    public Announcement update(@PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        return announcementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Map.of("message", "Announcement deleted");
    }

    @PatchMapping("/{id}/archive")
    public Announcement archive(@PathVariable Long id, @RequestBody ArchiveRequest request) {
        announcementService.archive(id, request.archived());
        return announcementService.getOrThrow(id);
    }

    @PatchMapping("/{id}/schedule")
    public Announcement schedule(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        return announcementService.updateSchedule(id, request);
    }

    @PostMapping("/{id}/jd")
    public Map<String, String> uploadJd(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Please choose a file to upload");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw ApiException.badRequest("Job description must be smaller than 5 MB");
        }
        try {
            announcementService.uploadJd(id, file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (java.io.IOException ex) {
            throw ApiException.badRequest("Could not read the uploaded file");
        }
        return Map.of("message", "Job description uploaded");
    }

    @GetMapping("/{id}/jd")
    public ResponseEntity<byte[]> downloadJd(@PathVariable Long id) {
        JdDownload file = announcementService.jd(id);
        String name = file.fileName() == null ? "job-description.pdf" : file.fileName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .contentType(file.mime() == null ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(file.mime()))
                .body(file.content());
    }

    @GetMapping("/{id}/applicants")
    public List<Map<String, Object>> applicants(@PathVariable Long id) {
        return announcementService.applicantRows(id);
    }

    @PostMapping("/{id}/results")
    public Map<String, String> postResults(@PathVariable Long id,
            @RequestBody ResultRequest request) {
        announcementService.postResults(id, request);
        return Map.of("message", "Results saved and students notified");
    }

    @GetMapping("/{id}/results")
    public Map<String, Object> results(@PathVariable Long id) {
        return announcementService.results(id);
    }
}
