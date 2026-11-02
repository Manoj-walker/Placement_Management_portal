package edu.nit.placement.model.announcement;

import edu.nit.placement.model.user.Student;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the eligibility engine (U4 / FR-05). */
class EligibilityCriteriaTest {

    private EligibilityCriteria criteria;
    private Student student;

    @BeforeEach
    void setUp() {
        criteria = new EligibilityCriteria();
        criteria.setBranches(Set.of("CSE", "ECE"));
        criteria.setMinCgpa(new BigDecimal("7.5"));
        criteria.setMaxBacklogs(0);
        criteria.setBatch("2026");

        student = new Student();
        student.setBranch("CSE");
        student.setCgpa(new BigDecimal("8.2"));
        student.setBacklogs(0);
        student.setBatch("2026");
    }

    @Test
    void eligibleStudentPassesEveryRule() {
        assertTrue(criteria.check(student));
    }

    @Test
    void studentFromUnlistedBranchIsRejected() {
        student.setBranch("MECH");
        assertFalse(criteria.check(student));
    }

    @Test
    void branchComparisonIsCaseInsensitive() {
        student.setBranch("cse");
        assertTrue(criteria.check(student));
    }

    @Test
    void cgpaBelowCutoffIsRejected() {
        student.setCgpa(new BigDecimal("7.49"));
        assertFalse(criteria.check(student));
    }

    @Test
    void cgpaEqualToCutoffIsAccepted() {
        student.setCgpa(new BigDecimal("7.50"));
        assertTrue(criteria.check(student));
    }

    @Test
    void tooManyBacklogsAreRejected() {
        student.setBacklogs(1);
        assertFalse(criteria.check(student));
    }

    @Test
    void differentBatchIsRejected() {
        student.setBatch("2025");
        assertFalse(criteria.check(student));
    }

    @Test
    void emptyBranchListMeansAllBranches() {
        criteria.setBranches(Set.of());
        student.setBranch("MECH");
        assertTrue(criteria.check(student));
    }

    @Test
    void announcementUsesItsCriteria() {
        Announcement announcement = new Announcement();
        announcement.setEligibility(criteria);
        assertTrue(announcement.isStudentEligible(student));
        student.setBranch("MECH");
        assertFalse(announcement.isStudentEligible(student));
    }
}
