package com.smartapply.assistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generate(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "[MOCK AI] Missing OpenAI API Key. Would have responded to: " + userMessage;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", "gpt-4o-mini"); // Using gpt-4o-mini for speed and cost-effectiveness
        request.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI API: " + e.getMessage();
        }

        return "Failed to generate AI response.";
    }

    public String tailorResume(String originalResume, String jobDescription) {
        String systemPrompt = "You are an expert resume writer. Given an original resume and a job description, tailor the resume to highlight the skills and experiences that best match the job description. Return ONLY the new tailored resume text, formatted professionally.";
        String userMessage = "Job Description:\n" + jobDescription + "\n\nOriginal Resume:\n" + originalResume;
        return generate(systemPrompt, userMessage);
    }

    public String generateCoverLetter(String originalResume, String jobDescription) {
        String systemPrompt = "You are an expert career coach. Given an original resume and a job description, write a compelling, professional cover letter for the candidate applying to this job. Keep it concise (3-4 paragraphs) and impactful. Return ONLY the cover letter text.";
        String userMessage = "Job Description:\n" + jobDescription + "\n\nResume Background:\n" + originalResume;
        return generate(systemPrompt, userMessage);
    }
}
