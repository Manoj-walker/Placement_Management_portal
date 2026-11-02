package edu.nit.placement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Admin account management payloads. */
public final class AdminDtos {

    private AdminDtos() {
    }

    public record CreateUserRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String role,
            String rollNo,
            String branch,
            String batch,
            @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
            Integer backlogs,
            String department) {
    }

    public record RoleRequest(@NotBlank String role) {
    }

    public record StatusRequest(@NotBlank String status) {
    }

    public record ResetPasswordRequest(@NotBlank String password) {
    }

    public record StatsResponse(
            long totalStudents,
            long totalCoordinators,
            long totalAdmins,
            long activeDrives,
            long totalDrives,
            long applications,
            long blockedUsers,
            long emailsSent,
            long unreadNotifications) {
    }

    public record CreateUserResponse(Long id, String email, String role) {
        public static CreateUserResponse of(edu.nit.placement.model.user.User user) {
            return new CreateUserResponse(user.getId(), user.getEmail(), user.getRole().name());
        }
    }
}
