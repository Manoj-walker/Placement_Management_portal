package edu.nit.placement.model.user;

import java.math.BigDecimal;

/** Student extends User — handles profile data used by the eligibility engine. */
public class Student extends User {

    private String rollNo;
    private String branch;
    private String batch;
    private BigDecimal cgpa;
    private int backlogs;

    public Student() {
        super(Role.STUDENT);
    }

    @Override
    public String dashboardKey() {
        return "student";
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = cgpa;
    }

    public int getBacklogs() {
        return backlogs;
    }

    public void setBacklogs(int backlogs) {
        this.backlogs = backlogs;
    }
}
