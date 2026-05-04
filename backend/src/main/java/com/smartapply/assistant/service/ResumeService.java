package com.smartapply.assistant.service;

import com.smartapply.assistant.dto.ResumeDto;
import com.smartapply.assistant.entity.AppUser;
import com.smartapply.assistant.entity.Resume;
import com.smartapply.assistant.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserService userService;

    public ResumeService(ResumeRepository resumeRepository, UserService userService) {
        this.resumeRepository = resumeRepository;
        this.userService = userService;
    }

    public List<ResumeDto> getAllResumes() {
        AppUser user = userService.getOrCreateDefaultUser();
        return resumeRepository.findByUserId(user.getId()).stream().map(r -> {
            ResumeDto dto = new ResumeDto();
            dto.setId(r.getId());
            dto.setName(r.getName());
            dto.setUploadedAt(r.getUploadedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public ResumeDto uploadResume(MultipartFile file) throws IOException {
        AppUser user = userService.getOrCreateDefaultUser();
        
        String textContent;
        if (file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
                org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                textContent = stripper.getText(document);
            }
        } else {
            textContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setName(file.getOriginalFilename());
        resume.setOriginalText(textContent);
        
        resume = resumeRepository.save(resume);

        ResumeDto dto = new ResumeDto();
        dto.setId(resume.getId());
        dto.setName(resume.getName());
        dto.setUploadedAt(resume.getUploadedAt());
        return dto;
    }
}
