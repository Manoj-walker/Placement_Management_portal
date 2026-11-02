package edu.nit.placement.model.announcement;

import java.time.LocalDate;

/** One recruitment round (online test, technical interview, HR, …). */
public class Round {

    private Long id;
    private int sequence;
    private String name;
    private LocalDate date;

    public Round() {
    }

    public Round(String name, LocalDate date, int sequence) {
        this.name = name;
        this.date = date;
        this.sequence = sequence;
    }

    public Round(String name, LocalDate date) {
        this(name, date, 0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
