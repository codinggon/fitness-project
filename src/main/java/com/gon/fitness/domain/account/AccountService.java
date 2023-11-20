package com.gon.fitness.domain.account;

import com.gon.fitness.web.account.UserAccount;
import com.gon.fitness.web.account.form.SignUpForm;
import com.gon.fitness.web.config.AppProperties;
import com.gon.fitness.web.mail.EmailMessage;
import com.gon.fitness.web.mail.EmailService;
import com.gon.fitness.web.settings.form.NotificationsForm;
import com.gon.fitness.web.settings.form.PasswordForm;
import com.gon.fitness.web.settings.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final JavaMailSender javaMailSender;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    public Account createAccountAndEmailCheck(SignUpForm signUpForm) {
        Account newAccount = createAccount(signUpForm);
        processEmailCheck(newAccount);
        return newAccount;
    }


    public void processEmailCheck(Account newAccount) {

        Account account = accountRepository.findByEmail(newAccount.getEmail());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(newAccount.getEmail());
            mimeMessageHelper.setSubject("피트니스, 회원 가입 인증");
            mimeMessageHelper.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                    "&email=" + newAccount.getEmail(),false);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


//        EmailMessage emailMessage = EmailMessage.builder()
//                .to(newAccount.getEmail())
//                .subject("스터디올래, 회원 가입 인증")
//                .message("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail())
//                .build();
//
//        emailService.sendEmail(emailMessage);
    }

    private Account createAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void completeSignUp(Account account) {
        account.setEmailVerified(true); //이메일 인증완료
        account.setJoinedAt(LocalDateTime.now());
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);

    }


    public void updateProfile(ProfileForm profileForm, Account account) {
        modelMapper.map(profileForm, account);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String usernameOrNickname) throws UsernameNotFoundException {

        Account account = accountRepository.findByEmail(usernameOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(usernameOrNickname);
        }
        if (account == null) {
            throw new UsernameNotFoundException(usernameOrNickname);
        }

        return new UserAccount(account);
    }

    public void updatePassword(Account account, PasswordForm passwordForm) {
        account.setPassword(passwordEncoder.encode(passwordForm.getNewPassword()));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, NotificationsForm notificationsForm) {
        modelMapper.map(notificationsForm, account);
        accountRepository.save(account);
    }
}























