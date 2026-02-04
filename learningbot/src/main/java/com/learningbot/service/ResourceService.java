package com.learningbot.service;

import com.learningbot.domain.Resource;
import com.learningbot.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
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

        boolean exists = resourceRepository.existsByNormalizedTitle(normalizedTitle);

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

    public List<Resource> getResourcesBySectionAndStudyTime(String section, int studyTime) {
        return resourceRepository.findBySectionAndStudyTime(section, studyTime);
    }

    public boolean titleExists(String title) {
        return resourceRepository.existsByNormalizedTitle(normalize(title));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        String lowercased = trimmed.toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lowercased, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
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
