package com.gon.fitness.web.event;

import com.gon.fitness.domain.account.Account;
import com.gon.fitness.domain.account.AccountRepository;
import com.gon.fitness.domain.account.AccountService;
import com.gon.fitness.web.account.CurrentAccount;
import com.gon.fitness.web.study.Study;
import com.gon.fitness.web.study.StudyRepository;
import com.gon.fitness.web.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final AccountService accountService;
    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {

        Account gon = accountRepository.findByNickname("gon");
        Study study = studyRepository.findStudyWithManagersByPath(path);// 매니저 , 스터디
        model.addAttribute(study);
        model.addAttribute(gon);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path, Model model,
                                 @Valid EventForm eventForm, Errors errors) {
        Account gon = accountRepository.findByNickname("gon");
        Study study = studyRepository.findStudyWithManagersByPath(path);
        if (errors.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(gon);
            return "event/form";
        }

        Event event = modelMapper.map(eventForm, Event.class);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        event.setCreatedBy(gon);

        eventRepository.save(event);

        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();

    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, Model model) {

        Account gon = accountRepository.findByNickname("gon");

        accountService.login(gon);

        Study study = studyRepository.findStudyWithManagersByPath(path);// 매니저 , 스터디
        model.addAttribute(study);
        model.addAttribute(gon);
        model.addAttribute(eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 " + id +"가 없습니다.")));
        return "event/view";
    }

    @PostMapping("/events/{id}/enroll")
    public String eventEnroll(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long id, Model model) {

        Account gon = accountRepository.findByNickname("gon");

        accountService.login(gon);
        Study study = studyRepository.findStudyOnlyByPath(path);
        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 " + id + "가 없습니다."));

        if (!enrollmentRepository.existsByEventAndAccount(event, gon)) {
            Enrollment enrollment = Enrollment.builder()
                    .enrolledAt(LocalDateTime.now())
                    .accepted(event.isAbleToWaitingEnrollment())
                    .account(gon)
                    .build();
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }

        return "event/view";
    }


    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account,
                                   @PathVariable String path, @PathVariable("id") Event event) {

        Account gon = accountRepository.findByNickname("gon");

        accountService.login(gon);
        Study study = studyRepository.findStudyOnlyByPath(path);

        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        //참석한게 아니면
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }

        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }




}













