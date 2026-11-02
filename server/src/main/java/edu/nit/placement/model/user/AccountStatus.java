package edu.nit.placement.model.user;

public enum AccountStatus {
    ACTIVE, BLOCKED;

    public static AccountStatus from(String value) {
        return AccountStatus.valueOf(value.trim().toUpperCase());
    }
}
