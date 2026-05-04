package com.smartapply.assistant.controller;

import com.smartapply.assistant.entity.ApplicationTracker;
import com.smartapply.assistant.service.ApplicationTrackerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tracker")
public class TrackerController {

    private final ApplicationTrackerService trackerService;

    public TrackerController(ApplicationTrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationTracker>> getTrackedApplications() {
        return ResponseEntity.ok(trackerService.getTrackedApplications());
    }
}
