package com.learningbot.service;

import com.learningbot.domain.Resource;
import com.learningbot.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public boolean addResource(ResourceDraft draft) {
        String normalizedTitle = normalize(draft.title());
        String normalizedAuthor = normalize(draft.author());
        String normalizedSection = normalize(draft.section());
        String normalizedFormat = normalize(draft.format());

        boolean exists = resourceRepository
                .existsByNormalizedTitleAndNormalizedAuthorAndNormalizedSectionAndNormalizedFormatAndStudyTime(
                        normalizedTitle,
                        normalizedAuthor,
                        normalizedSection,
                        normalizedFormat,
                        draft.studyTime()
                );

        if (exists) {
            return false;
        }

        Resource resource = new Resource();
        resource.setTitle(draft.title());
        resource.setAuthor(draft.author());
        resource.setSection(draft.section());
        resource.setFormat(draft.format());
        resource.setStudyTime(draft.studyTime());
        resource.setLink(draft.link());
        resource.setNormalizedTitle(normalizedTitle);
        resource.setNormalizedAuthor(normalizedAuthor);
        resource.setNormalizedSection(normalizedSection);
        resource.setNormalizedFormat(normalizedFormat);

        resourceRepository.save(resource);
        return true;
    }

    public List<Resource> getResourcesByStudyTime(int studyTime) {
        return resourceRepository.findByStudyTime(studyTime);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.toLowerCase(Locale.ROOT);
    }

    public record ResourceDraft(
            String title,
            String author,
            String section,
            String format,
            Integer studyTime,
            String link
    ) {
    }
}
