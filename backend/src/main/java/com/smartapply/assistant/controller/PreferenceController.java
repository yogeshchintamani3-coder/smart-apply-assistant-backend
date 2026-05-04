package com.smartapply.assistant.controller;

import com.smartapply.assistant.dto.PreferenceDto;
import com.smartapply.assistant.entity.JobPreference;
import com.smartapply.assistant.service.PreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<JobPreference> getPreferences() {
        return ResponseEntity.ok(preferenceService.getPreferences());
    }

    @PostMapping
    public ResponseEntity<JobPreference> updatePreferences(@RequestBody PreferenceDto dto) {
        return ResponseEntity.ok(preferenceService.updatePreferences(dto));
    }
}
