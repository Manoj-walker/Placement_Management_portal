package edu.nit.placement.model.announcement;

import edu.nit.placement.model.user.Student;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Encapsulates eligibility rules and answers check(student) for any drive.
 * Used by Announcement.isStudentEligible() and by the dashboard filter.
 */
public class EligibilityCriteria {

    private Set<String> branches = new LinkedHashSet<>();
    private BigDecimal minCgpa = BigDecimal.ZERO;
    private int maxBacklogs = 0;
    private String batch;

    public boolean check(Student student) {
        if (student == null) {
            return false;
        }
        if (branches != null && !branches.isEmpty()) {
            boolean branchOk = branches.stream().anyMatch(
                    b -> b.equalsIgnoreCase(student.getBranch()));
            if (!branchOk) {
                return false;
            }
        }
        if (minCgpa != null && student.getCgpa() != null
                && student.getCgpa().compareTo(minCgpa) < 0) {
            return false;
        }
        if (student.getBacklogs() > maxBacklogs) {
            return false;
        }
        return batch == null || batch.isBlank()
                || batch.equalsIgnoreCase(student.getBatch());
    }

    public Set<String> getBranches() {
        return branches;
    }

    public void setBranches(Set<String> branches) {
        this.branches = branches;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
        this.minCgpa = minCgpa;
    }

    public int getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(int maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }
}
