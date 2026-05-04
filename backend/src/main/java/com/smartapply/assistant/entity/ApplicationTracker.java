package com.smartapply.assistant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_tracker")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private String jobTitle;
    private String company;
    private String location;
    private String applicationUrl;
    
    // Status: Applied, Pending, Interview, Rejected
    private String status;
    
    private LocalDateTime appliedAt = LocalDateTime.now();
}
