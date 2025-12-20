package com.learningbot.domain;

import jakarta.persistence.*;
@Entity
@Table(name = "book")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    @Column(name = "section")
    private String section;
    private String format;
    @Column(name = "studytime")
    private Integer studyTime;
    @Column(name = "link")
    private String link;

    @Column(name = "normalized_title")
    private String normalizedTitle;

    @Column(name = "normalized_author")
    private String normalizedAuthor;

    @Column(name = "normalized_section")
    private String normalizedSection;

    @Column(name = "normalized_format")
    private String normalizedFormat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(Integer studyTime) {
        this.studyTime = studyTime;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public void setNormalizedTitle(String normalizedTitle) {
        this.normalizedTitle = normalizedTitle;
    }

    public String getNormalizedAuthor() {
        return normalizedAuthor;
    }

    public void setNormalizedAuthor(String normalizedAuthor) {
        this.normalizedAuthor = normalizedAuthor;
    }

    public String getNormalizedSection() {
        return normalizedSection;
    }

    public void setNormalizedSection(String normalizedSection) {
        this.normalizedSection = normalizedSection;
    }

    public String getNormalizedFormat() {
        return normalizedFormat;
    }

    public void setNormalizedFormat(String normalizedFormat) {
        this.normalizedFormat = normalizedFormat;
    }
}
