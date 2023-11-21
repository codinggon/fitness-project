package com.gon.fitness.web.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gon.fitness.domain.Tag;
import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.domain.settings.TagRepository;
import com.gon.fitness.web.account.CurrentAccount;
import com.gon.fitness.web.settings.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.Banner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final StudyService studyService;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount Account account, Model model) {

        Account gon = accountRepository.findByNickname("gon");

        model.addAttribute(gon);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentAccount Account account, Model model,
                                 @Valid StudyForm studyform, Errors errors) {

        Account gon = accountRepository.findByNickname("gon");

        if (studyRepository.existsByPath(studyform.getPath())) {
            errors.rejectValue("path", "wrong.path", "study path를 사용할 수 없습니다.");
        }

        if (errors.hasErrors()) {
            model.addAttribute(gon);
            return "study/form";
        }
        Study newStudy = studyService.createNewStudy(modelMapper.map(studyform, Study.class), gon);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
//        return "redirect:/study/" + Base64.getEncoder().encodeToString(newStudy.getPath().getBytes(StandardCharsets.UTF_8));
    }


    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentAccount Account account, Model model, @PathVariable String path) {

//        Account gon = accountRepository.findByNickname("gon");
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));

        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentAccount Account account, Model model, @PathVariable String path) {

//        Account gon = accountRepository.findByNickname("gon");
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));

        return "study/members";
    }

    @GetMapping("/study/{path}/banner")
    public String viewStudyBanner(@CurrentAccount Account account, Model model, @PathVariable String path) {

//        Account gon = accountRepository.findByNickname("gon");
        model.addAttribute(account);
        Study study = studyRepository.findByPath(path);
        model.addAttribute(study);

        return "study/banner";
    }

    //<form th:if="${!study.useBanner}" th:action="@{|/study/${study.getPath()}/banner/enable|}">
    @GetMapping("/study/{path}/banner/enable")
    public String viewStudyBannerEnable(@CurrentAccount Account account, Model model, @PathVariable String path) {

        Study study = studyService.getStudyByManager(account, path);
        studyService.enableStudyBanner(study);

        return "redirect:/";
    }

    @GetMapping("/study/{path}/tags")
    public String studyTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study study = studyService.getStudyToUpdateTag(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitles = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "study/tags";
    }


    @PostMapping("/study/{path}/tags/add")
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {

        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = studyService.findOrCreateTag(tagForm);
        studyService.addTag(study, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/study/{path}/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study/{path}/study")
    public String viewStudy(@CurrentAccount Account account, @PathVariable String path,Model model){

        Study study = studyRepository.findByPath(path);
        model.addAttribute(study);
        model.addAttribute(account);
        return "study/study";
    }

    @PostMapping("/study/{path}/settings/study/publish")
    public String publishStudy(@CurrentAccount Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Study study = studyRepository.findByPath(path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/study";
    }

    @PostMapping("/study/{path}/settings/study/close")
    public String closeStudy(@CurrentAccount Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Study study = studyRepository.findByPath(path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/study";
    }

    @PostMapping("/study/{path}/settings/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, Model model,
                               RedirectAttributes attributes) {
        Study study = studyRepository.findByPath(path);
//        if (!study.canUpdateRecruiting()) {
//            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
//            return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
//        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/study";
    }


    @PostMapping("/study/{path}/settings/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Study study = studyRepository.findByPath(path);
//        if (!study.canUpdateRecruiting()) {
//            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
//            return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
//        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/study";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.addMember(account, study);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }


    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.leaveMember(account, study);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }


}






















