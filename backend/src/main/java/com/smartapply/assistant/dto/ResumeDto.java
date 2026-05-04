package com.smartapply.assistant.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeDto {
    private Long id;
    private String name;
    private LocalDateTime uploadedAt;
}
