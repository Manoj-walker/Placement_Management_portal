package edu.nit.placement.model.announcement;

import edu.nit.placement.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Company announcement aggregate. Owns its eligibility rules and rounds
 * (composition) and answers isStudentEligible() through them.
 */
public class Announcement {

    private Long id;
    private String companyName;
    private String roleTitle;
    private String details;
    private BigDecimal ctc;
    private String location;
    private LocalDate lastDate;
    private LocalDate testDate;
    private LocalDate interviewDate;
    private String venue;
    private String testLink;
    private String jdFileName;
    private String jdMime;
    private boolean archived;
    private Long createdBy;

    private EligibilityCriteria eligibility = new EligibilityCriteria();
    private final List<Round> rounds = new ArrayList<>();

    // Transient per-student flags (filled by the dashboard query)
    private boolean eligible;
    private boolean applied;
    private boolean bookmarked;
    private boolean resultPosted;

    public boolean isStudentEligible(Student student) {
        return eligibility != null && eligibility.check(student);
    }

    /** A drive is closed once the last date has passed. */
    public boolean isClosed() {
        return lastDate != null && lastDate.isBefore(LocalDate.now());
    }

    public void archive() {
        this.archived = true;
    }

    public void restore() {
        this.archived = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public BigDecimal getCtc() {
        return ctc;
    }

    public void setCtc(BigDecimal ctc) {
        this.ctc = ctc;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public void setLastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getTestLink() {
        return testLink;
    }

    public void setTestLink(String testLink) {
        this.testLink = testLink;
    }

    public String getJdFileName() {
        return jdFileName;
    }

    public void setJdFileName(String jdFileName) {
        this.jdFileName = jdFileName;
    }

    public String getJdMime() {
        return jdMime;
    }

    public void setJdMime(String jdMime) {
        this.jdMime = jdMime;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public EligibilityCriteria getEligibility() {
        return eligibility;
    }

    public void setEligibility(EligibilityCriteria eligibility) {
        this.eligibility = eligibility;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isResultPosted() {
        return resultPosted;
    }

    public void setResultPosted(boolean resultPosted) {
        this.resultPosted = resultPosted;
    }
}
