package com.gon.fitness.web.study;

import com.gon.fitness.domain.Tag;
import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.settings.TagRepository;
import com.gon.fitness.web.settings.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final TagRepository tagRepository;


    public Study createNewStudy(Study study, Account account) {
        Study savedStudy = studyRepository.save(study);
        savedStudy.addManager(account);
        return savedStudy;
    }

    public Study getStudyByManager(Account account, String path) {
        Study study = studyRepository.findByPath(path);
        boolean isManager = study.getManagers().contains(account);
        checkIsManager(account, path, study);
        return study;
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public Tag findOrCreateTag(TagForm tagForm) {
        Tag tag = tagRepository.findByTitle(tagForm.getTitle());
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(tagForm.getTitle()).build());
        }
        return tag;
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkExistStudy(path, study);
        checkIsManager(account, path, study);
        return study;
    }

    private static void checkIsManager(Account account, String path, Study study) {
        if (!study.isManagerBy(account)) {
            throw new IllegalArgumentException(path + "에 해당하는 권한이 없습니다.");
        }
    }

    private static void checkExistStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 소터디가 없습니다.");
        }
    }


    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void publish(Study study) {
        study.setPublished(true);
    }

    public void close(Study study) {
        study.setPublished(false);
    }


    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }

    public void addMember(Account account, Study study) {
        study.getMembers().add(account);
    }


    public void leaveMember(Account account, Study study) {
        study.getMembers().remove(account);
    }

}
