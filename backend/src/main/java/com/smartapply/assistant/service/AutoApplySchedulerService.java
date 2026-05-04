package com.smartapply.assistant.service;

import com.smartapply.assistant.dto.JobDto;
import com.smartapply.assistant.entity.AppUser;
import com.smartapply.assistant.entity.JobPreference;
import com.smartapply.assistant.repository.ApplicationTrackerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoApplySchedulerService {

    private final JobScraperService jobScraperService;
    private final PreferenceService preferenceService;
    private final UserService userService;
    private final ApplicationTrackerRepository trackerRepository;
    private final ApplicationTrackerService trackerService;

    public AutoApplySchedulerService(JobScraperService jobScraperService, 
                                     PreferenceService preferenceService, 
                                     UserService userService, 
                                     ApplicationTrackerRepository trackerRepository,
                                     ApplicationTrackerService trackerService) {
        this.jobScraperService = jobScraperService;
        this.preferenceService = preferenceService;
        this.userService = userService;
        this.trackerRepository = trackerRepository;
        this.trackerService = trackerService;
    }

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void runAutoApply() {
        System.out.println("Starting daily auto-apply job...");
        
        JobPreference pref = preferenceService.getPreferences();
        if (!pref.isAutoApplyEnabled()) {
            System.out.println("Auto-apply is disabled for the default user.");
            return;
        }

        AppUser user = userService.getOrCreateDefaultUser();
        if (user.getNaukriEmail() == null || user.getNaukriPassword() == null) {
            System.out.println("Naukri credentials missing. Skipping auto-apply.");
            return;
        }

        // 1. Fetch matching jobs
        List<JobDto> jobs = jobScraperService.fetchJobs(pref);
        System.out.println("Found " + jobs.size() + " matching jobs.");

        int totalAppliedCount = 0;
        int skippedCount = 0;
        int batchSize = 10;
        int maxApplicationsPerDay = 100;

        // 2. Iterate and apply in batches
        for (int i = 0; i < jobs.size(); i++) {
            // Safety limit: 100 applications per day
            if (totalAppliedCount >= maxApplicationsPerDay) {
                System.out.println("Reached daily total safety limit of " + maxApplicationsPerDay + " applications.");
                break;
            }

            // Start of a new batch
            if (i % batchSize == 0) {
                System.out.println("Starting batch update for jobs " + (i + 1) + " to " + Math.min(i + batchSize, jobs.size()));
                try {
                    // Update profile before each batch to stay on top
                    jobScraperService.updateNaukriProfile(user.getNaukriEmail(), user.getNaukriPassword());
                } catch (Exception e) {
                    System.err.println("Profile update failed for this batch, but continuing with applications: " + e.getMessage());
                }
            }

            JobDto job = jobs.get(i);

            // Check if already applied
            if (trackerRepository.existsByUserIdAndApplicationUrl(user.getId(), job.getUrl())) {
                System.out.println("Already applied to: " + job.getTitle() + " at " + job.getCompany());
                skippedCount++;
                continue;
            }

            try {
                System.out.println("Auto-applying to: " + job.getTitle() + " at " + job.getCompany());
                jobScraperService.applyToNaukri(job, user.getNaukriEmail(), user.getNaukriPassword());
                trackerService.trackApplication(job.getTitle(), job.getCompany(), job.getLocation(), job.getUrl());
                totalAppliedCount++;
                
                // Small delay between jobs in a batch
                Thread.sleep(5000); 
            } catch (Exception e) {
                System.err.println("Failed to auto-apply for " + job.getTitle() + ": " + e.getMessage());
            }

            // End of batch pause
            if ((i + 1) % batchSize == 0 && (i + 1) < jobs.size()) {
                System.out.println("Batch complete. Pausing for 5 minutes to mimic human behavior...");
                try {
                    Thread.sleep(5 * 60 * 1000); // 5 minutes pause between batches
                } catch (InterruptedException e) {}
            }
        }

        System.out.println("Daily auto-apply job finished. Total Applied: " + totalAppliedCount + ", Total Skipped: " + skippedCount);
    }
}
