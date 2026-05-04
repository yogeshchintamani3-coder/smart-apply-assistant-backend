package com.smartapply.assistant.controller;

import com.smartapply.assistant.dto.AiRequestDto;
import com.smartapply.assistant.dto.AiResponseDto;
import com.smartapply.assistant.entity.Resume;
import com.smartapply.assistant.repository.ResumeRepository;
import com.smartapply.assistant.service.OpenAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OpenAiService openAiService;
    private final ResumeRepository resumeRepository;

    public AiController(OpenAiService openAiService, ResumeRepository resumeRepository) {
        this.openAiService = openAiService;
        this.resumeRepository = resumeRepository;
    }

    @PostMapping("/tailor-resume")
    public ResponseEntity<AiResponseDto> tailorResume(@RequestBody AiRequestDto request) {
        Resume resume = resumeRepository.findById(request.getResumeId()).orElse(null);
        if (resume == null) {
            return ResponseEntity.badRequest().body(new AiResponseDto("Resume not found."));
        }

        String result = openAiService.tailorResume(resume.getOriginalText(), request.getJobDescription());
        return ResponseEntity.ok(new AiResponseDto(result));
    }

    @PostMapping("/cover-letter")
    public ResponseEntity<AiResponseDto> generateCoverLetter(@RequestBody AiRequestDto request) {
        Resume resume = resumeRepository.findById(request.getResumeId()).orElse(null);
        if (resume == null) {
            return ResponseEntity.badRequest().body(new AiResponseDto("Resume not found."));
        }

        String result = openAiService.generateCoverLetter(resume.getOriginalText(), request.getJobDescription());
        return ResponseEntity.ok(new AiResponseDto(result));
    }
}
