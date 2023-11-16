package com.gon.fitness.domain.account;

import com.gon.fitness.web.account.UserAccount;
import com.gon.fitness.web.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;


    public Account createAccountAndEmailCheck(SignUpForm signUpForm) {
        Account newAccount = createAccount(signUpForm);
        processEmailCheck(newAccount);
        return newAccount;
    }


    private void processEmailCheck(Account newAccount) {
        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("스터디올래, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        javaMailSender.send(mailMessage);
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
}























