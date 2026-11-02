package edu.nit.placement.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.nit.placement.dao.AnnouncementDAO;
import edu.nit.placement.dao.ApplicationDAO;
import edu.nit.placement.dao.EmailLogDAO;
import edu.nit.placement.dao.NotificationDAO;
import edu.nit.placement.dao.UserDAO;
import edu.nit.placement.dto.AdminDtos.CreateUserRequest;
import edu.nit.placement.dto.AdminDtos.StatsResponse;
import edu.nit.placement.exception.ApiException;
import edu.nit.placement.model.user.AccountStatus;
import edu.nit.placement.model.user.Admin;
import edu.nit.placement.model.user.Coordinator;
import edu.nit.placement.model.user.Role;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;

/** Admin dashboard: statistics and account management. */
@Service
public class AdminService {

    private final UserDAO userDAO;
    private final AnnouncementDAO announcementDAO;
    private final ApplicationDAO applicationDAO;
    private final NotificationDAO notificationDAO;
    private final EmailLogDAO emailLogDAO;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public AdminService(UserDAO userDAO, AnnouncementDAO announcementDAO, ApplicationDAO applicationDAO,
            NotificationDAO notificationDAO, EmailLogDAO emailLogDAO,
            PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userDAO = userDAO;
        this.announcementDAO = announcementDAO;
        this.applicationDAO = applicationDAO;
        this.notificationDAO = notificationDAO;
        this.emailLogDAO = emailLogDAO;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public StatsResponse stats() {
        return new StatsResponse(
                userDAO.countByRole(Role.STUDENT),
                userDAO.countByRole(Role.COORDINATOR),
                userDAO.countByRole(Role.ADMIN),
                announcementDAO.countActive(),
                announcementDAO.countAll(),
                applicationDAO.countApplied(),
                userDAO.countByStatus(AccountStatus.BLOCKED),
                emailLogDAO.countSent(),
                notificationDAO.countUnreadAll());
    }

    public List<User> users(String role, String search) {
        return userDAO.findAll(role, search);
    }

    public User user(Long id) {
        return userDAO.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    /**
     * Admin utility: the stored BCrypt hash of a user's password. Deliberately
     * hidden from normal serialization (@JsonIgnore) — exposed only through
     * this endpoint, which is reachable only for ROLE_ADMIN.
     */
    public Map<String, Object> passwordHash(Long id) {
        User user = user(id);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", user.getId());
        row.put("name", user.getName());
        row.put("email", user.getEmail());
        row.put("role", user.getRole().name());
        row.put("status", user.getStatus().name());
        row.put("algorithm", "bcrypt");
        row.put("passwordHash", user.getPasswordHash());
        return row;
    }

    public User createUser(CreateUserRequest request) {
        Role role;
        try {
            role = Role.from(request.role());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Role must be STUDENT, COORDINATOR or ADMIN");
        }
        if (userDAO.existsByEmail(request.email())) {
            throw ApiException.badRequest("An account with this email already exists");
        }
        User user = UserDAO.newUserFor(role);
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (user instanceof Student student) {
            student.setRollNo(request.rollNo());
            student.setBranch(request.branch());
            student.setBatch(request.batch());
            student.setCgpa(request.cgpa());
            student.setBacklogs(request.backlogs() == null ? 0 : request.backlogs());
        } else if (user instanceof Coordinator coordinator) {
            coordinator.setDepartment(request.department());
        } else if (user instanceof Admin) {
            // no extra fields
        }
        userDAO.insert(user);
        notificationService.notifyUser(user,
                "Your Placement NIT account was created. Sign in with your college email.", "ACCOUNT");
        return user;
    }

    public User updateRole(Long id, String roleValue, User actor) {
        Role role;
        try {
            role = Role.from(roleValue);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Role must be STUDENT, COORDINATOR or ADMIN");
        }
        User user = user(id);
        if (user.getId().equals(actor.getId())) {
            throw ApiException.badRequest("You cannot change your own role");
        }
        userDAO.updateRole(id, role);
        return user(id);
    }

    public User updateStatus(Long id, String statusValue, User actor) {
        AccountStatus status;
        try {
            status = AccountStatus.from(statusValue);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Status must be ACTIVE or BLOCKED");
        }
        User user = user(id);
        if (user.getId().equals(actor.getId())) {
            throw ApiException.badRequest("You cannot block your own account");
        }
        userDAO.updateStatus(id, status);
        if (status == AccountStatus.ACTIVE) {
            notificationService.notifyUser(user, "Your account has been re-activated.", "ACCOUNT");
        }
        return user(id);
    }

    public void resetPassword(Long id, String password) {
        if (password == null || password.length() < 8) {
            throw ApiException.badRequest("Password must be at least 8 characters");
        }
        user(id);
        userDAO.updatePassword(id, passwordEncoder.encode(password));
    }

    public void deleteUser(Long id, User actor) {
        User user = user(id);
        if (user.getId().equals(actor.getId())) {
            throw ApiException.badRequest("You cannot delete your own account");
        }
        userDAO.delete(id);
    }
}
