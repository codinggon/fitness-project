package com.gon.fitness.web.settings;

import com.gon.fitness.domain.Tag;
import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.domain.settings.TagRepository;
import com.gon.fitness.web.settings.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final AccountRepository accountRepository;
    private final TagRepository tagRepository;


    public void addTags(TagForm tagForm, Account account) {
        String tagTitle = tagForm.getTitle();
        Tag tag = tagRepository.findByTitle(tagTitle);
        Tag savedTag = null;
        if (tag == null) {
            savedTag = tagRepository.save(Tag.builder().title(tagTitle).build());
            addTag(account, savedTag);
        }

        if (!account.isContainTag(tag)) {
            addTag(account, tag);
        }
    }


    public void removeTag(TagForm tagForm, Account account) {
        String tagTitle = tagForm.getTitle();
        Tag tag = tagRepository.findByTitle(tagTitle);
        if (tag != null) {
            removeTag(account, tag);
        }
    }

    private void addTag(Account account, Tag savedTag) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().add(savedTag));
    }

    private void removeTag(Account account, Tag tag) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().remove(tag));
    }


}
