package com.smartapply.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "job_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private String roles; // e.g., "Java Developer, Backend Engineer"
    private String locations; // e.g., "Remote, New York"
    private String experienceLevel; // e.g., "Mid-Senior"
    private String jobTypes; // e.g., "Full-time, Contract"
    private boolean autoApplyEnabled = false;
}
