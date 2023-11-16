package com.gon.fitness.web.account.validator;

import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.web.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email","invalid.email","메일이 중복입니다.");
        }
        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname","invalid.nickname","닉네임이 중복입니다.");
        }



    }
}
