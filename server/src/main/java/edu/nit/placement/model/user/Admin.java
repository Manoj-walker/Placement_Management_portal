package edu.nit.placement.model.user;

/** Admin extends User — manages accounts and roles. */
public class Admin extends User {

    public Admin() {
        super(Role.ADMIN);
    }

    @Override
    public String dashboardKey() {
        return "admin";
    }

    @Override
    public boolean canManageAnnouncements() {
        return true;
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }
}
