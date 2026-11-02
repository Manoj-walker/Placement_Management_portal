package edu.nit.placement.model.user;

/** Coordinator extends User — manages announcements, schedules and results. */
public class Coordinator extends User {

    private String department;

    public Coordinator() {
        super(Role.COORDINATOR);
    }

    @Override
    public String dashboardKey() {
        return "coordinator";
    }

    @Override
    public boolean canManageAnnouncements() {
        return true;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
