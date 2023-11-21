package com.gon.fitness.web.study;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    List<Study> findByKeyword(String keyword);

}
