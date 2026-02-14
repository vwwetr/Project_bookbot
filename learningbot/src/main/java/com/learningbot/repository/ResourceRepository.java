package com.learningbot.repository;

import com.learningbot.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    boolean existsByNormalizedTitle(String normalizedTitle);

    List<Resource> findByStudyTime(Integer studyTime);

    List<Resource> findBySectionAndStudyTime(String section, Integer studyTime);
}
