package edu.nit.placement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.nit.placement.dao.AnnouncementDAO;
import edu.nit.placement.dao.ApplicationDAO;
import edu.nit.placement.dao.ResultDAO;
import edu.nit.placement.dao.UserDAO;
import edu.nit.placement.dto.AnnouncementDtos.AnnouncementRequest;
import edu.nit.placement.dto.AnnouncementDtos.EligibilityDto;
import edu.nit.placement.dto.AnnouncementDtos.ResultRequest;
import edu.nit.placement.dto.AnnouncementDtos.RoundDto;
import edu.nit.placement.dto.AnnouncementDtos.ScheduleRequest;
import edu.nit.placement.exception.ApiException;
import edu.nit.placement.model.announcement.Announcement;
import edu.nit.placement.model.announcement.EligibilityCriteria;
import edu.nit.placement.model.announcement.Round;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;
import edu.nit.placement.storage.JdFileStore;

/** All announcement, application, schedule and result use cases. */
@Service
public class AnnouncementService {

    private final AnnouncementDAO announcementDAO;
    private final ApplicationDAO applicationDAO;
    private final ResultDAO resultDAO;
    private final UserDAO userDAO;
    private final NotificationService notificationService;
    private final JdFileStore jdFileStore;

    public AnnouncementService(AnnouncementDAO announcementDAO, ApplicationDAO applicationDAO,
            ResultDAO resultDAO, UserDAO userDAO, NotificationService notificationService,
            JdFileStore jdFileStore) {
        this.announcementDAO = announcementDAO;
        this.applicationDAO = applicationDAO;
        this.resultDAO = resultDAO;
        this.userDAO = userDAO;
        this.notificationService = notificationService;
        this.jdFileStore = jdFileStore;
    }

    // ---------------------------------------------------------------- create/update

    public Announcement create(AnnouncementRequest request, User creator) {
        Announcement announcement = new Announcement();
        apply(request, announcement);
        announcement.setCreatedBy(creator.getId());
        announcementDAO.insert(announcement);
        notifyEligible(announcement, "New drive published: " + announcement.getCompanyName()
                + " — " + announcement.getRoleTitle() + ". Last date: " + announcement.getLastDate());
        return announcement;
    }

    public Announcement update(Long id, AnnouncementRequest request) {
        Announcement announcement = getOrThrow(id);
        apply(request, announcement);
        announcementDAO.update(announcement);
        notifyEligible(announcement, "Drive updated: " + announcement.getCompanyName()
                + ". Check the latest eligibility and dates.");
        return getOrThrow(id);
    }

    private void apply(AnnouncementRequest request, Announcement announcement) {
        announcement.setCompanyName(request.companyName().trim());
        announcement.setRoleTitle(request.roleTitle().trim());
        announcement.setDetails(request.details());
        announcement.setCtc(request.ctc());
        announcement.setLocation(request.location());
        announcement.setLastDate(request.lastDate());
        announcement.setTestDate(request.testDate());
        announcement.setInterviewDate(request.interviewDate());
        announcement.setVenue(request.venue());
        announcement.setTestLink(request.testLink());

        EligibilityCriteria criteria = new EligibilityCriteria();
        EligibilityDto dto = request.eligibility();
        if (dto != null) {
            criteria.setBranches(dto.branches() == null ? new java.util.LinkedHashSet<>()
                    : new java.util.LinkedHashSet<>(dto.branches()));
            criteria.setMinCgpa(dto.minCgpa() == null ? BigDecimal.ZERO : dto.minCgpa());
            criteria.setMaxBacklogs(dto.maxBacklogs() == null ? 0 : dto.maxBacklogs());
            criteria.setBatch(dto.batch());
        }
        announcement.setEligibility(criteria);

        announcement.getRounds().clear();
        if (request.rounds() != null) {
            int seq = 1;
            for (RoundDto round : request.rounds()) {
                if (round.name() == null || round.name().isBlank()) {
                    continue;
                }
                announcement.getRounds().add(new Round(round.name(), round.date(), seq++));
            }
        }
    }

    // ---------------------------------------------------------------- queries

    public Announcement getOrThrow(Long id) {
        return announcementDAO.findById(id)
                .orElseThrow(() -> ApiException.notFound("Announcement not found"));
    }

    public List<Announcement> list(String search, boolean includeArchived) {
        List<Announcement> announcements = announcementDAO.search(search, includeArchived);
        announcements.forEach(a -> a.setResultPosted(!resultDAO.findByAnnouncement(a.getId()).isEmpty()));
        return announcements;
    }

    public void delete(Long id) {
        getOrThrow(id);
        announcementDAO.delete(id);
    }

    public void archive(Long id, boolean archived) {
        getOrThrow(id);
        announcementDAO.setArchived(id, archived);
    }

    /** A JD ready to download: bytes read from disk, mime fixed to octet-stream. */
    public record JdDownload(String fileName, String mime, byte[] content) {
    }

    public void uploadJd(Long id, String fileName, String mime, byte[] content) {
        getOrThrow(id);
        String storedPath = jdFileStore.save(id, fileName, content);
        announcementDAO.updateJd(id, fileName, storedPath);
    }

    public JdDownload jd(Long id) {
        getOrThrow(id);
        AnnouncementDAO.JdFile stored = announcementDAO.findJd(id)
                .orElseThrow(() -> ApiException.notFound("No job description uploaded for this drive"));
        byte[] content = stored.path() == null ? null : jdFileStore.read(stored.path());
        if (content == null) {
            throw ApiException.notFound("No job description uploaded for this drive");
        }
        return new JdDownload(stored.fileName(), "application/octet-stream", content);
    }

    // ---------------------------------------------------------------- student side

    public List<Announcement> dashboard(Student student, String search, String filter) {
        List<Announcement> announcements = announcementDAO.listForStudent(student.getId(), search);
        announcements.forEach(a -> a.setEligible(a.isStudentEligible(student)));
        if (filter == null || filter.isBlank() || "all".equalsIgnoreCase(filter)) {
            return announcements;
        }
        return announcements.stream().filter(a -> switch (filter.toLowerCase()) {
            case "eligible" -> a.isEligible();
            case "applied" -> a.isApplied();
            case "bookmarked" -> a.isBookmarked();
            case "closed" -> a.isClosed();
            case "open" -> !a.isClosed();
            default -> true;
        }).toList();
    }

    public Announcement detail(Student student, Long id) {
        Announcement announcement = getOrThrow(id);
        announcement.setEligible(announcement.isStudentEligible(student));
        String status = applicationDAO.statusesForStudent(student.getId()).get(id);
        announcement.setApplied("APPLIED".equalsIgnoreCase(status));
        announcement.setBookmarked("BOOKMARKED".equalsIgnoreCase(status));
        announcement.setResultPosted(!resultDAO.findByAnnouncement(id).isEmpty());
        return announcement;
    }

    public Announcement apply(Student student, Long id) {
        Announcement announcement = getOrThrow(id);
        if (announcement.isArchived()) {
            throw ApiException.badRequest("This drive has been archived");
        }
        if (!announcement.isStudentEligible(student)) {
            throw ApiException.forbidden("You do not meet the eligibility criteria for this drive");
        }
        if (announcement.isClosed()) {
            throw ApiException.badRequest("The application deadline has passed");
        }
        applicationDAO.setStatus(student.getId(), id, "APPLIED");
        notificationService.notifyUser(student,
                "Application submitted for " + announcement.getCompanyName() + " — " + announcement.getRoleTitle(),
                "APPLICATION");
        announcement.setEligible(true);
        announcement.setApplied(true);
        announcement.setBookmarked(false);
        return announcement;
    }

    public Announcement bookmark(Student student, Long id) {
        Announcement announcement = getOrThrow(id);
        if (applicationDAO.hasApplied(student.getId(), id)) {
            throw ApiException.badRequest("You have already applied to this drive");
        }
        applicationDAO.setStatus(student.getId(), id, "BOOKMARKED");
        announcement.setEligible(announcement.isStudentEligible(student));
        announcement.setBookmarked(true);
        announcement.setApplied(false);
        return announcement;
    }

    public void removeBookmark(Student student, Long id) {
        Map<Long, String> statuses = applicationDAO.statusesForStudent(student.getId());
        if ("BOOKMARKED".equalsIgnoreCase(statuses.get(id))) {
            applicationDAO.remove(student.getId(), id);
        }
    }

    public List<Announcement> applications(Student student) {
        return dashboard(student, null, "applied");
    }

    public List<Map<String, Object>> deadlines(Student student) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Announcement a : dashboard(student, null, "open")) {
            if (a.getLastDate() == null || a.getLastDate().isBefore(today)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", a.getId());
            row.put("companyName", a.getCompanyName());
            row.put("roleTitle", a.getRoleTitle());
            row.put("lastDate", a.getLastDate());
            row.put("daysLeft", ChronoUnit.DAYS.between(today, a.getLastDate()));
            row.put("applied", a.isApplied());
            row.put("bookmarked", a.isBookmarked());
            row.put("eligible", a.isEligible());
            row.put("testDate", a.getTestDate());
            row.put("interviewDate", a.getInterviewDate());
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> results(Student student) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Announcement a : dashboard(student, null, null)) {
            if (!a.isResultPosted()) {
                continue;
            }
            List<String> myResults = resultDAO.findByAnnouncement(a.getId()).stream()
                    .filter(r -> r.userId().equals(student.getId()))
                    .map(ResultDAO.ResultEntry::resultType)
                    .toList();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", a.getId());
            row.put("companyName", a.getCompanyName());
            row.put("roleTitle", a.getRoleTitle());
            row.put("resultPosted", true);
            row.put("outcome", myResults.isEmpty() ? "NOT_SHORTLISTED" : String.join(",", myResults));
            rows.add(row);
        }
        return rows;
    }

    // ---------------------------------------------------------------- coordinator side

    public List<Map<String, Object>> applicantRows(Long announcementId) {
        Announcement announcement = getOrThrow(announcementId);
        List<Long> appliedIds = applicationDAO.studentsApplied(announcementId);
        List<Long> shortlisted = resultDAO.userIds(announcementId, "SHORTLISTED");
        List<Long> selected = resultDAO.userIds(announcementId, "SELECTED");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Student student : userDAO.findStudentsByIds(appliedIds)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", student.getId());
            row.put("name", student.getName());
            row.put("email", student.getEmail());
            row.put("rollNo", student.getRollNo());
            row.put("branch", student.getBranch());
            row.put("batch", student.getBatch());
            row.put("cgpa", student.getCgpa());
            row.put("backlogs", student.getBacklogs());
            row.put("eligible", announcement.isStudentEligible(student));
            row.put("shortlisted", shortlisted.contains(student.getId()));
            row.put("selected", selected.contains(student.getId()));
            rows.add(row);
        }
        return rows;
    }

    public void postResults(Long announcementId, ResultRequest request) {
        Announcement announcement = getOrThrow(announcementId);
        String type = request.type() == null ? "" : request.type().trim().toUpperCase();
        if (!type.equals("SHORTLISTED") && !type.equals("SELECTED")) {
            throw ApiException.badRequest("Result type must be SHORTLISTED or SELECTED");
        }
        List<Long> userIds = request.userIds() == null ? List.of() : request.userIds();
        resultDAO.replace(announcementId, type, userIds);
        String label = type.equals("SELECTED") ? "selected" : "shortlisted";
        for (Student student : userDAO.findStudentsByIds(userIds)) {
            notificationService.notifyUser(student,
                    "You have been " + label + " for " + announcement.getCompanyName()
                            + " — " + announcement.getRoleTitle(),
                    "RESULT");
        }
    }

    public Map<String, Object> results(Long announcementId) {
        getOrThrow(announcementId);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shortlisted", resultDAO.userIds(announcementId, "SHORTLISTED"));
        payload.put("selected", resultDAO.userIds(announcementId, "SELECTED"));
        return payload;
    }

    public Announcement updateSchedule(Long id, ScheduleRequest request) {
        Announcement announcement = getOrThrow(id);
        if (request.lastDate() != null) {
            announcement.setLastDate(request.lastDate());
        }
        announcement.setTestDate(request.testDate());
        announcement.setInterviewDate(request.interviewDate());
        announcement.setVenue(request.venue());
        announcement.setTestLink(request.testLink());
        announcementDAO.update(announcement);
        List<Long> appliedIds = applicationDAO.studentsApplied(id);
        for (Student student : userDAO.findStudentsByIds(appliedIds)) {
            notificationService.notifyUser(student,
                    "Schedule updated for " + announcement.getCompanyName() + ". Test: "
                            + announcement.getTestDate() + ", Interview: " + announcement.getInterviewDate(),
                    "SCHEDULE");
        }
        return getOrThrow(id);
    }

    // ---------------------------------------------------------------- reminders

    /** Called by the scheduled reminder job for drives whose last date is tomorrow. */
    public void remindDeadlines(LocalDate date) {
        for (Announcement announcement : announcementDAO.findByDeadline(date)) {
            for (Student student : userDAO.findAllStudents()) {
                if (announcement.isStudentEligible(student)) {
                    notificationService.notifyUserOnce(student,
                            "Deadline reminder: " + announcement.getCompanyName() + " — "
                                    + announcement.getRoleTitle() + " closes on " + announcement.getLastDate(),
                            "REMINDER");
                }
            }
        }
    }

    private void notifyEligible(Announcement announcement, String message) {
        for (Student student : userDAO.findAllStudents()) {
            if (announcement.isStudentEligible(student)) {
                notificationService.notifyUser(student, message, "ANNOUNCEMENT");
            }
        }
    }
}
