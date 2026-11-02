package edu.nit.placement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.nit.placement.dto.AdminDtos.CreateUserRequest;
import edu.nit.placement.dto.AdminDtos.CreateUserResponse;
import edu.nit.placement.dto.AdminDtos.ResetPasswordRequest;
import edu.nit.placement.dto.AdminDtos.RoleRequest;
import edu.nit.placement.dto.AdminDtos.StatsResponse;
import edu.nit.placement.dto.AdminDtos.StatusRequest;
import edu.nit.placement.model.user.User;
import edu.nit.placement.service.AdminService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    public List<User> users(@RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {
        return adminService.users(role, search);
    }

    @GetMapping("/users/{id}")
    public User user(@PathVariable Long id) {
        return adminService.user(id);
    }

    /** BCrypt hash of the user's stored password (admin-only utility). */
    @GetMapping("/users/{id}/password-hash")
    public Map<String, Object> passwordHash(@PathVariable Long id) {
        return adminService.passwordHash(id);
    }

    @PostMapping("/users")
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return CreateUserResponse.of(adminService.createUser(request));
    }

    @PatchMapping("/users/{id}/role")
    public User updateRole(@PathVariable Long id, @RequestBody RoleRequest request,
            @AuthenticationPrincipal User actor) {
        return adminService.updateRole(id, request.role(), actor);
    }

    @PatchMapping("/users/{id}/status")
    public User updateStatus(@PathVariable Long id, @RequestBody StatusRequest request,
            @AuthenticationPrincipal User actor) {
        return adminService.updateStatus(id, request.status(), actor);
    }

    @PostMapping("/users/{id}/reset-password")
    public Map<String, String> resetPassword(@PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        adminService.resetPassword(id, request.password());
        return Map.of("message", "Password reset");
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> delete(@PathVariable Long id, @AuthenticationPrincipal User actor) {
        adminService.deleteUser(id, actor);
        return Map.of("message", "User deleted");
    }
}
