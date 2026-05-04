package com.smartapply.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private String title;
    private String company;
    private String location;
    private String url;
    private String descriptionSnippet;
}
