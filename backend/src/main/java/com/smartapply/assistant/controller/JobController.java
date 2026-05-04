package com.smartapply.assistant.controller;

import com.smartapply.assistant.dto.JobDto;
import com.smartapply.assistant.entity.JobPreference;
import com.smartapply.assistant.service.ApplicationTrackerService;
import com.smartapply.assistant.service.JobScraperService;
import com.smartapply.assistant.service.PreferenceService;
import com.smartapply.assistant.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobScraperService jobScraperService;
    private final PreferenceService preferenceService;
    private final ApplicationTrackerService trackerService;
    private final UserService userService;

    public JobController(JobScraperService jobScraperService, PreferenceService preferenceService, 
                         ApplicationTrackerService trackerService, UserService userService) {
        this.jobScraperService = jobScraperService;
        this.preferenceService = preferenceService;
        this.trackerService = trackerService;
        this.userService = userService;
    }

    @GetMapping("/match")
    public ResponseEntity<List<JobDto>> findMatchingJobs() {
        JobPreference pref = preferenceService.getPreferences();
        return ResponseEntity.ok(jobScraperService.fetchJobs(pref));
    }

    @PostMapping("/apply")
    public ResponseEntity<String> autoApply(@RequestBody JobDto job) {
        // 1. Fetch user and credentials
        var user = userService.getOrCreateDefaultUser();
        
        // 2. Perform actual application on Naukri if credentials exist
        if (user.getNaukriEmail() != null && user.getNaukriPassword() != null) {
            try {
                jobScraperService.applyToNaukri(job, user.getNaukriEmail(), user.getNaukriPassword());
                trackerService.trackApplication(job.getTitle(), job.getCompany(), job.getLocation(), job.getUrl());
                return ResponseEntity.ok("Successfully applied to " + job.getTitle() + " on Naukri.com");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Naukri automation failed: " + e.getMessage());
            }
        } else {
            // Fallback to just tracking if no credentials
            trackerService.trackApplication(job.getTitle(), job.getCompany(), job.getLocation(), job.getUrl());
            return ResponseEntity.ok("Application tracked locally. Please provide Naukri credentials in Preferences for automated applying.");
        }
    }
}
