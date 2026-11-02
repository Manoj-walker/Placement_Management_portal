package edu.nit.placement.model.user;

public enum Role {
    STUDENT, COORDINATOR, ADMIN;

    public static Role from(String value) {
        return Role.valueOf(value.trim().toUpperCase());
    }
}
