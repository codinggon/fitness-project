package com.gon.fitness.web.settings;

import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.domain.account.AccountService;
import com.gon.fitness.web.account.CurrentAccount;
import com.gon.fitness.web.settings.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentAccount Account account, Model model){

        Account byEmail = accountRepository.findByEmail(account.getEmail());

        model.addAttribute(byEmail);
        model.addAttribute("profile",modelMapper.map(byEmail, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String profileUpdate(@CurrentAccount Account account, @Valid ProfileForm profileForm, Errors errors, Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        Account byNickname = accountRepository.findByNickname(account.getNickname());

        accountService.updateProfile(profileForm, byNickname);

        redirectAttributes.addFlashAttribute("message","수정되었습니다.");
        return "redirect:/settings/profile";
    }




}










