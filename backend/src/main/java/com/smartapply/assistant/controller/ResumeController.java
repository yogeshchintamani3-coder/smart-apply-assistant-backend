package com.smartapply.assistant.controller;

import com.smartapply.assistant.dto.ResumeDto;
import com.smartapply.assistant.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping
    public ResponseEntity<List<ResumeDto>> getResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeDto> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(resumeService.uploadResume(file));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
