package com.learningbot.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String topic;
    private String format;
    private Integer durationMin;
    private String source;
    private String fileUrl;

    private LocalDateTime createdAt = LocalDateTime.now();
}