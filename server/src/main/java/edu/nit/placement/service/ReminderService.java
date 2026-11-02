package edu.nit.placement.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Daily reminder job. Every morning it notifies eligible students about drives
 * whose application deadline is tomorrow (SRS U5 / FR-08).
 */
@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final AnnouncementService announcementService;

    public ReminderService(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Running placement reminder job for deadlines on {}", tomorrow);
        announcementService.remindDeadlines(tomorrow);
    }
}
