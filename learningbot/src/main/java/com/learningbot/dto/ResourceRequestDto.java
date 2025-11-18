package com.learningbot.dto;

import jakarta.validation.constraints.*;

public record ResourceRequestDto(
        @NotBlank String title,
        @NotBlank String topic,
        @NotBlank String format,
        @Min(5) @Max(120) int durationMin,
        @NotBlank String source,
        String fileUrl
) {}