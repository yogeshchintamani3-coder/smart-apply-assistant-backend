package com.smartapply.assistant.service;

import com.smartapply.assistant.dto.PreferenceDto;
import com.smartapply.assistant.entity.AppUser;
import com.smartapply.assistant.entity.JobPreference;
import com.smartapply.assistant.repository.JobPreferenceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PreferenceService {

    private final JobPreferenceRepository preferenceRepository;
    private final UserService userService;

    public PreferenceService(JobPreferenceRepository preferenceRepository, UserService userService) {
        this.preferenceRepository = preferenceRepository;
        this.userService = userService;
    }

    public JobPreference getPreferences() {
        AppUser user = userService.getOrCreateDefaultUser();
        return preferenceRepository.findByUserId(user.getId()).orElseGet(() -> {
            JobPreference defaultPref = new JobPreference();
            defaultPref.setUser(user);
            return defaultPref;
        });
    }

    public JobPreference updatePreferences(PreferenceDto dto) {
        AppUser user = userService.getOrCreateDefaultUser();
        JobPreference pref = preferenceRepository.findByUserId(user.getId()).orElse(new JobPreference());
        
        pref.setUser(user);
        pref.setRoles(dto.getRoles());
        pref.setLocations(dto.getLocations());
        pref.setExperienceLevel(dto.getExperienceLevel());
        pref.setJobTypes(dto.getJobTypes());
        pref.setAutoApplyEnabled(dto.isAutoApplyEnabled());
        
        // Update user credentials
        user.setNaukriEmail(dto.getNaukriEmail());
        user.setNaukriPassword(dto.getNaukriPassword());
        userService.save(user);
        
        return preferenceRepository.save(pref);
    }
}
