package com.smartapply.assistant.dto;

import lombok.Data;

@Data
public class PreferenceDto {
    private String roles;
    private String locations;
    private String experienceLevel;
    private String jobTypes;
    private String naukriEmail;
    private String naukriPassword;
    private boolean autoApplyEnabled;
}
