package com.learningbot.dto;

import jakarta.validation.constraints.*;

public record ResourceRequestDto(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String section,
        @NotBlank String format,
        @Min(5) @Max(120) int studyTime,
        String link
) {}
