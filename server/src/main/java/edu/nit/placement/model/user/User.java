package edu.nit.placement.model.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class of the user hierarchy (encapsulation: passwordHash is private).
 * Student, Coordinator and Admin inherit from this class.
 */
public abstract class User {

    private Long id;
    private String name;
    private String email;
    private String passwordHash; // never exposed through a public getter for serialization
    private Role role;
    private AccountStatus status = AccountStatus.ACTIVE;
    private LocalDateTime createdAt;

    protected User(Role role) {
        this.role = role;
    }

    /** Polymorphism: every subclass reports the dashboard it owns. */
    @JsonProperty("dashboardKey")
    public abstract String dashboardKey();

    /** Polymorphism: role-specific capability checks. */
    public boolean canManageAnnouncements() {
        return false;
    }

    public boolean canManageUsers() {
        return false;
    }

    public boolean isBlocked() {
        return status == AccountStatus.BLOCKED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
