package edu.nit.placement.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.nit.placement.dao.UserDAO;
import edu.nit.placement.dto.AuthDtos.AuthResponse;
import edu.nit.placement.dto.AuthDtos.LoginRequest;
import edu.nit.placement.dto.AuthDtos.RegisterRequest;
import edu.nit.placement.dto.StudentDtos.PasswordChangeRequest;
import edu.nit.placement.dto.StudentDtos.ProfileUpdateRequest;
import edu.nit.placement.exception.ApiException;
import edu.nit.placement.model.user.Coordinator;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;
import edu.nit.placement.security.JwtService;

@Service
public class AuthService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userDAO.existsByEmail(request.email())) {
            throw ApiException.badRequest("An account with this email already exists");
        }
        Student student = new Student();
        student.setName(request.name().trim());
        student.setEmail(request.email().trim().toLowerCase());
        student.setPasswordHash(passwordEncoder.encode(request.password()));
        student.setRollNo(request.rollNo().trim());
        student.setBranch(request.branch().trim());
        student.setBatch(request.batch().trim());
        student.setCgpa(request.cgpa());
        student.setBacklogs(request.backlogs());
        userDAO.insert(student);
        return new AuthResponse(jwtService.generate(student), student);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userDAO.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (user.isBlocked()) {
            throw ApiException.forbidden("Your account has been blocked. Contact the placement cell.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        return new AuthResponse(jwtService.generate(user), user);
    }

    public User currentUser(User principal) {
        return userDAO.findById(principal.getId())
                .orElseThrow(() -> ApiException.unauthorized("Session expired, please sign in again"));
    }

    public User updateProfile(User principal, ProfileUpdateRequest request) {
        User user = currentUser(principal);
        if (user instanceof Student student) {
            if (request.name() != null && !request.name().isBlank()) {
                student.setName(request.name().trim());
            }
            if (request.rollNo() != null) {
                student.setRollNo(request.rollNo().trim());
            }
            if (request.branch() != null) {
                student.setBranch(request.branch().trim());
            }
            if (request.batch() != null) {
                student.setBatch(request.batch().trim());
            }
            if (request.cgpa() != null) {
                student.setCgpa(request.cgpa());
            }
            if (request.backlogs() != null) {
                student.setBacklogs(request.backlogs());
            }
            userDAO.updateProfile(student);
        } else {
            if (request.name() != null && !request.name().isBlank()) {
                user.setName(request.name().trim());
            }
            if (user instanceof Coordinator coordinator && request.branch() != null) {
                coordinator.setDepartment(request.branch().trim());
            }
            userDAO.updateBasicProfile(user);
        }
        return currentUser(user);
    }

    public void changePassword(User principal, PasswordChangeRequest request) {
        User user = currentUser(principal);
        if (request.currentPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        if (request.newPassword() == null || request.newPassword().length() < 8) {
            throw ApiException.badRequest("New password must be at least 8 characters");
        }
        userDAO.updatePassword(user.getId(), passwordEncoder.encode(request.newPassword()));
    }
}
