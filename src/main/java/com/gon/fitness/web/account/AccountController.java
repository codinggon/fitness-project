package com.gon.fitness.web.account;

import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.domain.account.AccountService;
import com.gon.fitness.web.account.form.SignUpForm;
import com.gon.fitness.web.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final AccountService accountService;


//    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }


    @GetMapping("sign-up")
    public String signUpForm(@ModelAttribute("signUpForm") SignUpForm signUpForm) {
        return "account/sign-up";
    }

    @PostMapping("sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {

        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        Account account = accountService.createAccountAndEmailCheck(signUpForm);
        accountService.login(account);
        //회원 가입 처리
        return "redirect:/";
    }



    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {

        Account account = accountRepository.findByEmail(email);

        if (account == null) {
            model.addAttribute("error","wrong.email");
            return "account/checked-email";
        }

        if (!account.isValidToken(token)) {
            model.addAttribute("error","wrong.token");
            return "account/checked-email";
        }

        //email을 통한 회원가입 검증완료 처리
        accountService.completeSignUp(account);

        model.addAttribute("numberOfUser",accountRepository.count());
        model.addAttribute("nickname",account.getNickname());
        return "account/checked-email";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("email",account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String confirmEmail(@CurrentAccount Account account, Model model) {

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error","1시간에 한번만 전송 가능");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        return "/";
    }


}









