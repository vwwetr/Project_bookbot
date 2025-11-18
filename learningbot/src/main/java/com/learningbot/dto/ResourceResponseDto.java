package com.learningbot.dto;

import java.time.LocalDateTime;

public record ResourceResponseDto(
        Long id,
        String title,
        String topic,
        String format,
        int durationMin,
        String source,
        String fileUrl,
        LocalDateTime createdAt
) {}