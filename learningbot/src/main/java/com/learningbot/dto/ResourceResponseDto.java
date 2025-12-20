package com.learningbot.dto;

public record ResourceResponseDto(
        Long id,
        String title,
        String author,
        String section,
        String format,
        int studyTime,
        String link
) {}
