package edu.nit.placement.dto;

import java.math.BigDecimal;

import edu.nit.placement.model.user.User;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request/response shapes for authentication and registration. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72, message = "must be 8-72 characters") String password,
            @NotBlank String rollNo,
            @NotBlank String branch,
            @NotBlank String batch,
            @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
            @NotNull @DecimalMin("0") Integer backlogs) {
    }

    public record AuthResponse(String token, User user) {
    }
}
