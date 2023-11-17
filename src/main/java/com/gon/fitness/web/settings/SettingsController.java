package com.gon.fitness.web.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gon.fitness.domain.Tag;
import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.domain.account.AccountService;
import com.gon.fitness.domain.settings.TagRepository;
import com.gon.fitness.web.account.CurrentAccount;
import com.gon.fitness.web.settings.form.NotificationsForm;
import com.gon.fitness.web.settings.form.PasswordForm;
import com.gon.fitness.web.settings.form.ProfileForm;
import com.gon.fitness.web.settings.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.Banner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.management.Notification;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final TagRepository tagRepository;
    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @GetMapping("/settings/tags")
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        Account gon = accountRepository.findByNickname("gon");
        model.addAttribute(gon);
        List<String> getTags = gon.getTags().stream().map(Tag::getTitle).collect(Collectors.toList());

        List<String> allTags = tagRepository.findAll().stream().map(tag -> tag.getTitle()).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));
        model.addAttribute("tags",getTags);

        return "settings/tags";
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public String updateTagsAdd(@RequestBody TagForm tagForm, Model model) {

        Account account = accountRepository.findByNickname("gon");

        String tagTitle = tagForm.getTitle();
        Tag tag = tagRepository.findByTitle(tagTitle);
        Tag savedTag = null;
        if (tag == null) {
            savedTag = tagRepository.save(Tag.builder().title(tagTitle).build());
            account.getTags().add(savedTag);
            accountRepository.save(account);
        }

        if (!account.getTags().contains(tag)) {
            account.getTags().add(tag);
            accountRepository.save(account);
        }

        return "ok";
    }

    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity updateTagsRemove(@RequestBody TagForm tagForm, Model model) {

        Account account = accountRepository.findByNickname("gon");

        String tagTitle = tagForm.getTitle();
        Tag tag = tagRepository.findByTitle(tagTitle);
        if (tag != null) {
            account.getTags().remove(tag);
            accountRepository.save(account);
        }else{
            return ResponseEntity.badRequest().body(tagTitle+"이 없습니다.");
        }
        return ResponseEntity.ok().build();
    }






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

    @GetMapping("/settings/password")
    public String passwordUpdateForm(@CurrentAccount Account account, Model model) {

        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }
    @PostMapping("/settings/password")
    public String passwordUpdateSubmit(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,Model model,
        RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm);
        redirectAttributes.addFlashAttribute("message","비번이 변경되었습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute("notifications",modelMapper.map(account, NotificationsForm.class));
        return "/settings/notification";
    }

    @PostMapping("/settings/notifications")
    public String updateNotificationsSubmit(@CurrentAccount Account account, Model model
        ,NotificationsForm notificationsForm, RedirectAttributes redirectAttributes) {


        accountService.updateNotifications(account, notificationsForm);

        redirectAttributes.addFlashAttribute("message","수정 완료");
        return "redirect:/settings/notifications";
    }







}












