package edu.nit.placement.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/** Student profile and password payloads. */
public final class StudentDtos {

    private StudentDtos() {
    }

    public record ProfileUpdateRequest(
            String name,
            String rollNo,
            String branch,
            String batch,
            @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
            Integer backlogs) {
    }

    public record PasswordChangeRequest(
            String currentPassword,
            String newPassword) {
    }
}
