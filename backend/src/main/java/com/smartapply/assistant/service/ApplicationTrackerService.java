package com.smartapply.assistant.service;

import com.smartapply.assistant.entity.AppUser;
import com.smartapply.assistant.entity.ApplicationTracker;
import com.smartapply.assistant.repository.ApplicationTrackerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationTrackerService {

    private final ApplicationTrackerRepository trackerRepository;
    private final UserService userService;

    public ApplicationTrackerService(ApplicationTrackerRepository trackerRepository, UserService userService) {
        this.trackerRepository = trackerRepository;
        this.userService = userService;
    }

    public List<ApplicationTracker> getTrackedApplications() {
        AppUser user = userService.getOrCreateDefaultUser();
        return trackerRepository.findByUserIdOrderByAppliedAtDesc(user.getId());
    }

    public ApplicationTracker trackApplication(String title, String company, String location, String url) {
        AppUser user = userService.getOrCreateDefaultUser();
        
        ApplicationTracker tracker = new ApplicationTracker();
        tracker.setUser(user);
        tracker.setJobTitle(title);
        tracker.setCompany(company);
        tracker.setLocation(location);
        tracker.setApplicationUrl(url);
        tracker.setStatus("Applied");
        tracker.setAppliedAt(LocalDateTime.now());
        
        return trackerRepository.save(tracker);
    }
}
