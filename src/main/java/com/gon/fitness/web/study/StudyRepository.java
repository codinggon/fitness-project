package com.gon.fitness.web.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> , StudyRepositoryExtension{
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags","zones","managers","members"}, type= EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags","managers"})
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"members"})
    Study findStudyWithMembersByPath(String path);

    @EntityGraph(attributePaths = {"managers"})
    Study findStudyWithManagersByPath(String path);


    Study findStudyOnlyByPath(String path);


}
