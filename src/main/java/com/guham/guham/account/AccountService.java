package com.guham.guham.account;

import com.guham.guham.config.AppProperties;
import com.guham.guham.domain.Account;
import com.guham.guham.domain.Tag;
import com.guham.guham.domain.Zone;
import com.guham.guham.mail.EmailMessage;
import com.guham.guham.mail.EmailService;
import com.guham.guham.settings.form.Notifications;
import com.guham.guham.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;


    public Account signUp(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmMail(newAccount);
        return newAccount;
    }

    // Controller + 재전송 호출 시 Managed Entity 넘기기
    public void sendSignUpConfirmMail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "팀빌딩구함 서비스를 이용하려면 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage email = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("구함, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(email);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = signUpForm.toEntity();
        account.generateEmailCheckToken();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void logIn(Account account) {
        // LawPassWord 이용하지 않기 위한 방법
        syncAuthenticationAccount(account);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nicknameOrEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(nicknameOrEmail);
        if (account == null) {
            account = accountRepository.findByNickname(nicknameOrEmail);
        }

        if (account == null) {
            throw new UsernameNotFoundException(nicknameOrEmail);
        }
        return new UserAccount(account);
    }


    public void completeSignUp(Account account) {
        account.completeSignUp();
        logIn(account);
    }

    private void syncAuthenticationAccount(Account account) {
        Authentication token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(token);
    }


    public void updateProfile(Long accountId, Profile profile) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateProfile(profile);
        syncAuthenticationAccount(findAccount);
    }

    public void updatePassword(Long accountId, String newPassword) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updatePassword(passwordEncoder.encode(newPassword));
        syncAuthenticationAccount(findAccount);
    }

    public void updateNotifications(Long accountId, Notifications notifications) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateNotifications(notifications);
        syncAuthenticationAccount(findAccount);
    }

    public void updateNickname(Long accountId, String nickname) {
        Account findAccount = accountRepository.findById(accountId)
                .orElseThrow(EntityNotFoundException::new);
        findAccount.updateNickname(nickname);
        syncAuthenticationAccount(findAccount);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();

        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "팀빌딩 구함 로그인");
        context.setVariable("message", "로그인하려면 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage email = EmailMessage.builder()
                .to(account.getEmail())
                .subject("팀빌딩구함, 로그인 링크")
                .message(message)
                .build();

        emailService.sendEmail(email);
    }

    public void addTag(Long accountId, Tag tag) {
        Optional<Account> byId = accountRepository.findById(accountId);
        byId.ifPresent(account -> account.getTags().add(tag));
    }

    public Set<Tag> getTag(Long accountId) {
        Optional<Account> byId = accountRepository.findById(accountId);
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Long accountId, Tag tag) {
        Optional<Account> byId = accountRepository.findById(accountId);
        byId.ifPresent(account -> account.getTags().remove(tag));
    }

    public Set<Zone> getZone(Long accountId) {
        Optional<Account> byId = accountRepository.findById(accountId);
        return byId.orElseThrow().getZones();
    }

    public void addZone(Long accountId, Zone zone) {
        Optional<Account> byId = accountRepository.findById(accountId);
        byId.ifPresent(account -> account.getZones().add(zone));
    }

    public void removeZone(Long accountId, Zone zone) {
        Optional<Account> byId = accountRepository.findById(accountId);
        byId.ifPresent(account -> account.getZones().remove(zone));
    }

    public Account getAccount(String nickname){
        Account byNickname = accountRepository.findByNickname(nickname);
        if(byNickname == null){
            throw new IllegalArgumentException(nickname +  "에 해당하는 사용자가 없습니다.");
        }
        return byNickname;
    }
}
