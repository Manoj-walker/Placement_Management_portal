package edu.nit.placement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Announcement create/update payloads and coordinator actions. */
public final class AnnouncementDtos {

    private AnnouncementDtos() {
    }

    public record EligibilityDto(
            Set<String> branches,
            @DecimalMin("0.0") BigDecimal minCgpa,
            Integer maxBacklogs,
            String batch) {
    }

    public record RoundDto(
            @NotBlank String name,
            LocalDate date) {
    }

    public record AnnouncementRequest(
            @NotBlank String companyName,
            @NotBlank String roleTitle,
            String details,
            @NotNull @DecimalMin("0.0") BigDecimal ctc,
            String location,
            @NotNull LocalDate lastDate,
            LocalDate testDate,
            LocalDate interviewDate,
            String venue,
            String testLink,
            @Valid EligibilityDto eligibility,
            @Valid List<RoundDto> rounds) {
    }

    public record ScheduleRequest(
            LocalDate lastDate,
            LocalDate testDate,
            LocalDate interviewDate,
            String venue,
            String testLink) {
    }

    public record ResultRequest(
            @NotBlank String type,
            List<Long> userIds) {
    }

    public record ArchiveRequest(boolean archived) {
    }
}
